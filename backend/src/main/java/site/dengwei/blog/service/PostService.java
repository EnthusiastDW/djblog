package site.dengwei.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import site.dengwei.blog.dto.PostDetailDTO;
import site.dengwei.blog.dto.request.*;
import site.dengwei.blog.entity.Post;

import java.util.List;
import java.util.Map;

/**
 * 文章服务接口
 *
 * @author dengwei
 * @since 2025-09-08 11:56:27
 */
public interface PostService extends IService<Post> {

    /**
     * 根据ID查询文章，不存在则抛出异常
     */
    Post getByIdOrThrow(Long id);

    /**
     * 根据ID查询文章详情，不存在则抛出异常
     */
    PostDetailDTO getPostDetailOrThrow(Long id);

    /**
     * 搜索文章
     */
    Page<Post> searchPosts(String keyword, Page<Post> page);

    /**
     * 分页查询草稿文章
     */
    Page<Post> selectDrafts(Page<Post> page);

    /**
     * 获取文章归档列表（按年月分组）
     */
    List<Map<String, Object>> getPostArchives();

    /**
     * 根据年月查询文章列表
     */
    Page<Post> getPostsByArchive(String yearMonth, Page<Post> page);

    /**
     * 获取文章详情及其关联信息
     */
    PostDetailDTO getPostDetail(Long postId);

    /**
     * 保存草稿
     */
    boolean saveDraft(SaveDraftRequest request);

    /**
     * 发布文章
     */
    boolean publish(PublishRequest request);

    /**
     * 创建文章
     */
    boolean createPost(CreatePostRequest request);

    /**
     * 更新文章
     */
    boolean updatePost(UpdatePostRequest request);

    /**
     * 删除文章
     */
    boolean deletePosts(List<Long> idList);
}
