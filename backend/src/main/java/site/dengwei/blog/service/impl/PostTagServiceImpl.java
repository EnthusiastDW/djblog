package site.dengwei.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dengwei.blog.entity.Post;
import site.dengwei.blog.entity.PostTag;
import site.dengwei.blog.mapper.PostTagMapper;
import site.dengwei.blog.service.PostTagService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 文章-标签关联表服务实现类
 *
 * @author dengwei
 * @since 2025-09-10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostTagServiceImpl extends ServiceImpl<PostTagMapper, PostTag> implements PostTagService {

    private final PostTagMapper postTagMapper;

    @Override
    public List<Post> getPostsByTagId(Long tagId) {
        log.debug("根据标签ID查询文章列表，标签ID: {}", tagId);
        return postTagMapper.selectPostsByTagId(tagId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setPostTags(Long postId, List<Long> tagIds) {
        log.info("为文章设置标签，文章ID: {}, 标签IDs: {}", postId, tagIds);

        // 先删除文章原有的所有标签关联
        removeByPostId(postId);

        // 如果没有新标签，直接返回
        if (tagIds == null || tagIds.isEmpty()) {
            log.debug("文章标签列表为空，文章ID: {}", postId);
            return;
        }

        // 批量插入新的标签关联
        List<PostTag> postTags = tagIds.stream()
                .map(tagId -> {
                    PostTag postTag = new PostTag();
                    postTag.setPostId(postId);
                    postTag.setTagId(tagId);
                    return postTag;
                })
                .collect(Collectors.toList());

        this.saveBatch(postTags);
        log.info("文章标签设置成功，文章ID: {}, 标签数量: {}", postId, postTags.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByPostId(Long postId) {
        log.debug("删除文章的所有标签关联，文章ID: {}", postId);
        postTagMapper.deleteByPostId(postId);
    }
}
