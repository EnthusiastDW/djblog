package site.dengwei.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import site.dengwei.blog.dto.PostDetailDTO;
import site.dengwei.blog.dto.PostListDTO;
import site.dengwei.blog.dto.request.*;
import site.dengwei.blog.entity.Post;
import site.dengwei.blog.enums.PostStatus;
import site.dengwei.blog.service.AiSummaryService;
import site.dengwei.blog.service.PostService;
import site.dengwei.blog.util.LambdaQueryUtils;
import site.dengwei.blog.dto.Response;

import java.util.List;
import java.util.Map;

/**
 * 文章控制器
 *
 * @author dengwei
 * @since 2025-09-08 11:56:26
 */
@Slf4j
@RestController
@RequestMapping("post")
@RequiredArgsConstructor
public class PostController {
    
    private final PostService postService;
    private final AiSummaryService aiSummaryService;

    /**
     * 分页查询所有文章
     */
    @GetMapping
    public Response<Page<PostListDTO>> selectAll(Page<Post> page, Post post) {
        return Response.success(postService.getPostListWithRelations(page, post));
    }

    /**
     * 分页查询所有文章（管理后台，包含草稿）
     */
    @GetMapping("/admin/list")
    public Response<Page<PostListDTO>> selectAllForAdmin(Page<Post> page, Post post) {
        return Response.success(postService.getAllPostListWithRelations(page, post));
    }

    /**
     * 分页查询草稿文章
     */
    @GetMapping("/drafts")
    public Response<Page<Post>> selectDrafts(Page<Post> page) {
        return Response.success(postService.selectDrafts(page));
    }

    /**
     * 搜索文章
     */
    @GetMapping("/search")
    public Response<Page<Post>> search(
            Page<Post> page,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String summary,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) PostStatus status) {
        return Response.success(postService.searchPostsAdvanced(keyword, title, summary, categoryId, tagId, authorId, page, status));
    }

    /**
     * 根据 ID 查询文章
     */
    @GetMapping("{id}")
    public Response<Post> selectOne(@PathVariable Long id) {
        return Response.success(postService.getByIdOrThrow(id));
    }
    
    /**
     * 根据 slug 查询文章详情（SEO 友好 URL）
     */
    @GetMapping("/slug/{slug}")
    public Response<PostDetailDTO> getBySlug(@PathVariable String slug) {
        return Response.success(postService.getPostBySlugOrThrow(slug));
    }

    /**
     * 查询文章归档列表（按年分组）
     */
    @GetMapping("/archives")
    public Response<List<Map<String, Object>>> getArchives(
            @RequestParam(defaultValue = "10") Integer size) {
        return Response.success(postService.getPostArchivesByYear(size));
    }

    /**
     * 根据年月查询文章列表
     */
    @GetMapping("/archives/{yearMonth}")
    public Response<Page<Post>> getPostsByArchive(
            @PathVariable @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "年月格式不正确，应为 yyyy-MM") String yearMonth,
            Page<Post> page) {
        return Response.success(postService.getPostsByArchive(yearMonth, page));
    }

    /**
     * 查询文章详情
     */
    @GetMapping("{id}/detail")
    public Response<PostDetailDTO> selectDetail(@PathVariable Long id) {
        return Response.success(postService.getPostDetailOrThrow(id));
    }

    /**
     * 保存草稿
     */
    @PostMapping("/draft")
    public Response<Boolean> saveDraft(@Valid @RequestBody SaveDraftRequest request) {
        return Response.success(postService.saveDraft(request));
    }

    /**
     * 发布文章
     */
    @PostMapping("/publish")
    public Response<Boolean> publish(@Valid @RequestBody PublishRequest request) {
        return Response.success(postService.publish(request));
    }

    /**
     * 创建文章
     */
    @PostMapping
    public Response<Boolean> insert(@Valid @RequestBody CreatePostRequest request) {
        return Response.success(postService.createPost(request));
    }

    /**
     * 更新文章
     */
    @PutMapping
    public Response<Boolean> update(@Valid @RequestBody UpdatePostRequest request) {
        return Response.success(postService.updatePost(request));
    }

    /**
     * 删除文章
     */
    @DeleteMapping
    public Response<Boolean> delete(@RequestParam List<Long> idList) {
        return Response.success(postService.deletePosts(idList));
    }

    /**
     * AI生成摘要
     */
    @PostMapping("/summary/generate")
    public Response<String> generateSummary(@RequestBody SummaryRequest request) {
        String summary = aiSummaryService.generateSummary(
                request.getTitle(), 
                request.getContent(), 
                request.getMaxLength() != null ? request.getMaxLength() : 200
        );
        return Response.success(summary);
    }

    @GetMapping("/stats/views")
    public Response<Long> getTotalViewCount() {
        return Response.success(postService.getTotalViewCount());
    }

    @PostMapping("/{id}/view")
    public Response<Boolean> incrementViewCount(@PathVariable Long id) {
        return Response.success(postService.incrementViewCount(id));
    }

    /**
     * 摘要生成请求
     */
    @lombok.Data
    public static class SummaryRequest {
        private String title;
        private String content;
        private Integer maxLength;
    }
}
