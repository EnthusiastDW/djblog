package site.dengwei.blog.service.platform.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import site.dengwei.blog.entity.Post;
import site.dengwei.blog.entity.PostPlatformSync;
import site.dengwei.blog.enums.PlatformSyncStatus;
import site.dengwei.blog.mapper.PostMapper;
import site.dengwei.blog.mapper.PostPlatformSyncMapper;
import site.dengwei.blog.service.platform.PlatformPublisher;
import site.dengwei.blog.service.platform.PlatformSyncOrchestrator;
import site.dengwei.blog.service.platform.dto.PlatformSyncRequest;
import site.dengwei.blog.service.platform.dto.PlatformSyncResult;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformSyncOrchestratorImpl implements PlatformSyncOrchestrator {

    private final List<PlatformPublisher> publishers;
    private final PostPlatformSyncMapper postPlatformSyncMapper;
    private final PostMapper postMapper;

    // 每篇文章每个平台的同步锁，防止重复同步
    private final ConcurrentHashMap<String, Lock> syncLocks = new ConcurrentHashMap<>();

    @Async("platformSyncExecutor")
    @Override
    public void syncPost(Long postId, String[] platforms) {
        log.info("开始同步文章 {} 到平台: {}", postId, Arrays.toString(platforms));
        Post post = postMapper.selectById(postId);
        if (post == null) {
            log.error("文章 {} 不存在", postId);
            return;
        }

        PlatformSyncRequest request = buildRequest(post);
        Set<String> targetPlatforms = Set.of(platforms);

        for (PlatformPublisher publisher : publishers) {
            String platformCode = publisher.getPlatformCode();
            if (!targetPlatforms.contains(platformCode)) {
                continue;
            }

            String lockKey = postId + ":" + platformCode;
            Lock lock = syncLocks.computeIfAbsent(lockKey, k -> new ReentrantLock());
            lock.lock();
            try {
                syncToPlatform(post, publisher, request);
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public PlatformSyncResult syncPostToPlatform(Long postId, String platformCode) {
        log.info("手动同步文章 {} 到平台: {}", postId, platformCode);
        Post post = postMapper.selectById(postId);
        if (post == null) {
            return PlatformSyncResult.failed(platformCode, "文章不存在");
        }

        Optional<PlatformPublisher> publisherOpt = publishers.stream()
                .filter(p -> p.getPlatformCode().equals(platformCode))
                .findFirst();

        if (publisherOpt.isEmpty()) {
            return PlatformSyncResult.failed(platformCode, "未找到平台发布器: " + platformCode);
        }

        String lockKey = postId + ":" + platformCode;
        Lock lock = syncLocks.computeIfAbsent(lockKey, k -> new ReentrantLock());
        lock.lock();
        try {
            // 检查是否正在同步中
            PostPlatformSync existing = postPlatformSyncMapper.selectOne(
                    new LambdaQueryWrapper<PostPlatformSync>()
                            .eq(PostPlatformSync::getPostId, postId)
                            .eq(PostPlatformSync::getPlatformCode, platformCode)
            );
            if (existing != null && existing.getSyncStatus() == PlatformSyncStatus.SYNCING) {
                log.warn("文章 {} 正在同步到平台 {} 中，拒绝重复请求", postId, platformCode);
                return PlatformSyncResult.failed(platformCode, "该平台正在同步中，请勿重复操作");
            }

            PlatformSyncRequest request = buildRequest(post);
            return syncToPlatform(post, publisherOpt.get(), request);
        } finally {
            lock.unlock();
        }
    }

    private PlatformSyncResult syncToPlatform(Post post, PlatformPublisher publisher, PlatformSyncRequest request) {
        String platformCode = publisher.getPlatformCode();

        if (!publisher.isEnabled()) {
            log.warn("平台 {} 未配置或未启用，跳过同步（未设置对应环境变量）", platformCode);
            return PlatformSyncResult.failed(platformCode, "平台未配置，请设置环境变量");
        }

        if (!publisher.validateConfig()) {
            log.warn("平台 {} 配置无效，跳过同步", platformCode);
            return PlatformSyncResult.failed(platformCode, "平台配置无效");
        }

        PostPlatformSync record = getOrCreateSyncRecord(post.getId(), platformCode);
        record.setSyncStatus(PlatformSyncStatus.SYNCING);
        postPlatformSyncMapper.updateById(record);

        try {
            PlatformSyncResult result = publisher.publish(request);

            if (result.isSuccess()) {
                record.setSyncStatus(PlatformSyncStatus.SUCCESS);
                record.setExternalUrl(result.getExternalUrl());
                record.setSyncedAt(LocalDateTime.now());
                record.setErrorMessage(null);
                postPlatformSyncMapper.updateById(record);

                appendSyncPlatform(post, platformCode);

                log.info("文章 {} 同步到 {} 成功: {}", post.getId(), platformCode, result.getExternalUrl());
            } else {
                record.setSyncStatus(PlatformSyncStatus.FAILED);
                record.setErrorMessage(result.getErrorMessage());
                postPlatformSyncMapper.updateById(record);

                log.error("文章 {} 同步到 {} 失败: {}", post.getId(), platformCode, result.getErrorMessage());
            }
            return result;

        } catch (Exception e) {
            log.error("文章 {} 同步到 {} 异常", post.getId(), platformCode, e);
            record.setSyncStatus(PlatformSyncStatus.FAILED);
            record.setErrorMessage(e.getMessage());
            postPlatformSyncMapper.updateById(record);

            return PlatformSyncResult.failed(platformCode, e.getMessage());
        }
    }

    private PostPlatformSync getOrCreateSyncRecord(Long postId, String platformCode) {
        PostPlatformSync record = postPlatformSyncMapper.selectOne(
                new LambdaQueryWrapper<PostPlatformSync>()
                        .eq(PostPlatformSync::getPostId, postId)
                        .eq(PostPlatformSync::getPlatformCode, platformCode)
        );
        if (record == null) {
            record = new PostPlatformSync();
            record.setPostId(postId);
            record.setPlatformCode(platformCode);
            record.setSyncStatus(PlatformSyncStatus.PENDING);
            postPlatformSyncMapper.insert(record);
        }
        return record;
    }

    private void appendSyncPlatform(Post post, String platformCode) {
        String existing = post.getSyncPlatforms();
        if (existing == null || existing.isEmpty()) {
            post.setSyncPlatforms(platformCode);
        } else {
            Set<String> platforms = Arrays.stream(existing.split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet());
            platforms.add(platformCode);
            post.setSyncPlatforms(String.join(",", platforms));
        }
        postMapper.updateById(post);
    }

    private PlatformSyncRequest buildRequest(Post post) {
        PlatformSyncRequest request = new PlatformSyncRequest();
        request.setPostId(post.getId());
        request.setTitle(post.getTitle());
        request.setContent(post.getContent());
        request.setSummary(post.getSummary());
        request.setSlug(post.getSlug());

        List<String> tags = postMapper.selectTagsByPostId(post.getId())
                .stream()
                .map(tag -> tag.getName())
                .collect(Collectors.toList());
        request.setTags(tags);

        return request;
    }
}
