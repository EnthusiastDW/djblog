package site.dengwei.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dengwei.blog.entity.PostVisitStatistics;
import site.dengwei.blog.mapper.PostMapper;
import site.dengwei.blog.mapper.PostVisitStatisticsMapper;
import site.dengwei.blog.service.PostVisitStatisticsService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文章访问统计服务实现类
 *
 * @author dengwei
 * @since 2026-04-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostVisitStatisticsServiceImpl extends ServiceImpl<PostVisitStatisticsMapper, PostVisitStatistics> implements PostVisitStatisticsService {

    private final PostVisitStatisticsMapper postVisitStatisticsMapper;
    private final PostMapper postMapper;

    /**
     * 缓存：文章ID -> 访问量
     * 使用 ConcurrentHashMap 保证线程安全
     */
    private final Map<Long, Long> viewCountCache = new ConcurrentHashMap<>();

    /**
     * 脏数据集合：记录浏览量发生变化的文章ID
     * 用于增量同步，避免全量更新
     */
    private final Set<Long> dirtyPostIds = ConcurrentHashMap.newKeySet();

    @Override
    public void recordPostVisit(Long postId, String visitorId) {
        if (postId == null || visitorId == null || visitorId.isEmpty()) {
            return;
        }

        LocalDate today = LocalDate.now();

        try {
            // 尝试插入数据库（唯一索引保证去重）
            PostVisitStatistics statistics = new PostVisitStatistics();
            statistics.setPostId(postId);
            statistics.setVisitorId(visitorId);
            statistics.setVisitDate(today);
            save(statistics);

            // 更新缓存中的浏览量
            viewCountCache.merge(postId, 1L, Long::sum);
            
            // 标记为脏数据（需要同步到数据库）
            dirtyPostIds.add(postId);

            log.debug("记录访问成功：PostId={}, VisitorId={}", postId, visitorId);
        } catch (DuplicateKeyException e) {
            // 防止并发重复插入，忽略即可（数据库唯一索引已保证去重）
            log.debug("访问已记录（并发或重复）：PostId={}, VisitorId={}", postId, visitorId);
        }
    }

    @Override
    @Async("postVisitStatisticsExecutor")
    public void recordPostVisitAsync(Long postId, String visitorId) {
        // 异步执行，调用同步方法
        recordPostVisit(postId, visitorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncViewCountToPostTable() {
        log.info("开始同步浏览量到文章表...");
        
        Set<Long> currentDirtyIds = null;
        try {
            if (dirtyPostIds.isEmpty()) {
                log.debug("没有需要同步的文章");
                return;
            }
            
            // 立即获取并清除脏数据，最小化并发窗口
            currentDirtyIds = Set.copyOf(dirtyPostIds);
            dirtyPostIds.removeAll(currentDirtyIds);
            
            int updatedCount = 0;
            
            // 同步已获取的文章浏览量
            for (Long postId : currentDirtyIds) {
                Long viewCount = viewCountCache.get(postId);
                if (viewCount != null) {
                    // 更新 post 表的 view_count 字段
                    postMapper.updateViewCount(postId, viewCount);
                    updatedCount++;
                }
            }
            
            log.info("同步浏览量到文章表完成，更新了 {} 篇文章", updatedCount);
        } catch (Exception e) {
            log.error("同步浏览量到文章表失败", e);
            // 如果同步失败，将脏数据标记恢复，下次继续同步
            if (currentDirtyIds != null) {
                dirtyPostIds.addAll(currentDirtyIds);
            }
            throw e; // 抛出异常以便事务回滚
        }
    }

    @Override
    public void initCache() {
        log.info("初始化文章访问统计缓存...");
        
        try {
            // 从数据库加载所有文章的访问量
            List<Map<String, Object>> results = postVisitStatisticsMapper.batchCountPostVisitors();
            
            for (Map<String, Object> row : results) {
                Long postId = ((Number) row.get("post_id")).longValue();
                Long viewCount = ((Number) row.get("view_count")).longValue();
                viewCountCache.put(postId, viewCount);
            }
            
            log.info("文章访问统计缓存初始化完成，共 {} 篇文章", viewCountCache.size());
        } catch (Exception e) {
            log.error("初始化文章访问统计缓存失败", e);
        }
    }
}
