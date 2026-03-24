package site.dengwei.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import site.dengwei.blog.entity.Post;
import site.dengwei.blog.entity.PostTag;

import java.util.List;

/**
 * 文章-标签关联表服务接口
 *
 * @author dengwei
 * @since 2025-09-10
 */
public interface PostTagService extends IService<PostTag> {

    /**
     * 根据标签ID查询关联的文章列表
     * @param tagId 标签ID
     * @return 文章列表
     */
    List<Post> getPostsByTagId(Long tagId);

    /**
     * 为文章设置标签
     * @param postId 文章ID
     * @param tagIds 标签ID列表
     */
    void setPostTags(Long postId, List<Long> tagIds);

    /**
     * 根据文章ID删除所有关联的标签
     * @param postId 文章ID
     */
    void removeByPostId(Long postId);
}
