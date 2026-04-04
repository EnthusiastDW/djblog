package site.dengwei.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import site.dengwei.blog.dto.PostDetailDTO;
import site.dengwei.blog.dto.PostListDTO;
import site.dengwei.blog.dto.request.*;
import site.dengwei.blog.entity.Post;
import site.dengwei.blog.enums.PostStatus;

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
     * 根据 ID 查询文章详情，不存在则抛出异常
     */
    PostDetailDTO getPostDetailOrThrow(Long id);
    
    /**
     * 根据 slug 查询文章详情，不存在则抛出异常
     */
    PostDetailDTO getPostBySlugOrThrow(String slug);

    /**
     * 搜索文章
     */
    Page<Post> searchPosts(String keyword, Page<Post> page);

    /**
     * 高级搜索（支持标题、摘要、标签、分类、作者）
     */
    Page<Post> searchPostsAdvanced(String keyword, String title, String summary, Long categoryId, Long tagId, Long authorId, Page<Post> page, PostStatus status);
    
    /**
     * 获取文章列表（带分类和标签信息）
     */
    Page<PostListDTO> getPostListWithRelations(Page<Post> page, Post queryParam);
    
    /**
     * 获取所有文章列表（管理后台用，不过滤状态）
     */
    Page<PostListDTO> getAllPostListWithRelations(Page<Post> page, Post queryParam);
    
    /**
     * 分页查询草稿文章
     */
    Page<Post> selectDrafts(Page<Post> page);

    /**
     * 获取文章归档列表（按年月分组）
     */
    List<Map<String, Object>> getPostArchives();

    /**
     * 获取文章归档列表（按年分组，每年内按月份分页）
     */
    List<Map<String, Object>> getPostArchivesByYear(Integer size);

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

    /**
     * AI 生成摘要
     */
    String generateSummary(String title, String content, int maxLength);

    /**
     * 获取已发布文章的总阅读量
     */
    Long getTotalViewCount();

    /**
     * 增加文章阅读量
     */
    boolean incrementViewCount(Long id);

    /**
     * 获取热门文章列表（按访问量排序）
     * @param limit 数量限制
     * @return 热门文章列表
     */
    List<Post> getPopularPosts(int limit);

    /**
     * 批量导入文章
     *
     * @param posts 文章列表
     * @param userId 用户 ID
     * @return 导入结果（成功数量、失败数量）
     */
    Map<String, Object> importPosts(List<ImportPostRequest> posts, Long userId);
}
