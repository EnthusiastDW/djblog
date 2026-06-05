package site.dengwei.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import site.dengwei.blog.dto.PostDetailDTO;
import site.dengwei.blog.dto.PostListDTO;
import site.dengwei.blog.dto.Response;
import site.dengwei.blog.dto.request.*;
import site.dengwei.blog.entity.Post;
import site.dengwei.blog.enums.PostStatus;
import site.dengwei.blog.entity.PostPlatformSync;
import site.dengwei.blog.service.AiSummaryService;
import site.dengwei.blog.service.PostPlatformSyncService;
import site.dengwei.blog.service.PostService;
import site.dengwei.blog.service.PostVisitStatisticsService;
import site.dengwei.blog.service.platform.PlatformSyncOrchestrator;
import site.dengwei.blog.service.platform.dto.PlatformSyncResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 文章控制器
 *
 * @author dengwei
 * @since 2025-09-08 11:56:26
 */
@Tag(name = "Post", description = "文章管理 — 文章 CRUD、搜索、归档、导入、同步")
@Slf4j
@RestController
@RequestMapping("post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final AiSummaryService aiSummaryService;
    private final PostVisitStatisticsService postVisitStatisticsService;
    private final PlatformSyncOrchestrator platformSyncOrchestrator;
    private final PostPlatformSyncService postPlatformSyncService;

    /**
     * 分页查询所有文章
     */
    @Operation(summary = "文章列表", description = "分页查询已发布的文章列表（前台使用）")
    @GetMapping
    public Response<Page<PostListDTO>> selectAll(Page<Post> page, Post post) {
        return Response.success(postService.getPostListWithRelations(page, post));
    }

    /**
     * 按分类ID列表查询文章（用于包含子分类的文章查询）
     */
    @Operation(summary = "按分类查询文章", description = "根据多个分类ID查询文章（含子分类）")
    @GetMapping("/by-categories")
    public Response<Page<PostListDTO>> selectByCategoryIds(
            Page<Post> page,
            @RequestParam List<Long> categoryIds) {
        return Response.success(postService.getPostListWithRelationsByCategoryIds(page, categoryIds));
    }

    /**
     * 分页查询所有文章（管理后台，包含草稿）
     */
    @Operation(summary = "管理后台文章列表", description = "管理后台使用，包含所有状态的文章（含草稿、已删除）")
    @GetMapping("/admin/list")
    public Response<Page<PostListDTO>> selectAllForAdmin(Page<Post> page, Post post) {
        return Response.success(postService.getAllPostListWithRelations(page, post));
    }

    /**
     * 分页查询草稿文章
     */
    @Operation(summary = "草稿列表", description = "分页查询草稿状态的文章")
    @GetMapping("/drafts")
    public Response<Page<Post>> selectDrafts(Page<Post> page) {
        return Response.success(postService.selectDrafts(page));
    }

    /**
     * 搜索文章
     */
    @Operation(summary = "搜索文章", description = "根据关键词、标题、摘要、分类、标签等多条件组合搜索")
    @GetMapping("/search")
    public Response<Page<PostListDTO>> search(
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
    @Operation(summary = "根据ID查询文章", description = "根据文章ID获取文章基本信息")
    @GetMapping("{id}")
    public Response<Post> selectOne(@PathVariable Long id) {
        return Response.success(postService.getByIdOrThrow(id));
    }

    /**
     * 根据 slug 查询文章详情(SEO 友好 URL)
     */
    @Operation(summary = "根据Slug查询文章", description = "通过 URL 友好的 Slug 标识获取文章详情，同时记录访问统计")
    @GetMapping("/slug/{slug}")
    public Response<PostDetailDTO> getBySlug(@PathVariable String slug, HttpServletRequest request) {
        PostDetailDTO post = postService.getPostBySlugOrThrow(slug);

        // 记录文章访问统计
        try {
            String visitorId = request.getHeader("X-Visitor-ID");
            if (visitorId != null && !visitorId.isEmpty()) {
                postVisitStatisticsService.recordPostVisit(post.getId(), visitorId);
            }
        } catch (Exception e) {
            // 忽略统计异常，不影响主流程
            log.debug("记录文章访问统计失败", e);
        }

        return Response.success(post);
    }

    /**
     * 查询文章归档列表（按年分组）
     */
    @Operation(summary = "文章归档", description = "按年份分组查看文章归档列表")
    @GetMapping("/archives")
    public Response<List<Map<String, Object>>> getArchives(
            @RequestParam(defaultValue = "10") Integer size) {
        return Response.success(postService.getPostArchivesByYear(size));
    }

    /**
     * 根据年月查询文章列表
     */
    @Operation(summary = "按年月查询文章", description = "根据指定的年月（yyyy-MM）获取该月的文章列表")
    @GetMapping("/archives/{yearMonth}")
    public Response<Page<PostListDTO>> getPostsByArchive(
            @PathVariable @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "年月格式不正确，应为 yyyy-MM") String yearMonth,
            Page<Post> page) {
        return Response.success(postService.getPostsByArchive(yearMonth, page));
    }

    /**
     * 查询文章详情
     */
    @Operation(summary = "文章详情", description = "获取文章的完整信息，包括分类、标签等关联数据")
    @GetMapping("{id}/detail")
    public Response<PostDetailDTO> selectDetail(@PathVariable Long id) {
        return Response.success(postService.getPostDetailOrThrow(id));
    }

    /**
     * 保存草稿
     */
    @Operation(summary = "保存草稿", description = "保存文章为草稿状态，不公开显示")
    @PostMapping("/draft")
    public Response<Long> saveDraft(@Valid @RequestBody SaveDraftRequest request) {
        return Response.success(postService.saveDraft(request));
    }

    /**
     * 发布文章
     */
    @Operation(summary = "发布文章", description = "直接将文章发布为公开状态")
    @PostMapping("/publish")
    public Response<Long> publish(@Valid @RequestBody PublishRequest request) {
        return Response.success(postService.publish(request));
    }

    /**
     * 创建文章
     */
    @Operation(summary = "创建文章", description = "创建新文章，可指定状态（草稿/发布）")
    @PostMapping
    public Response<Boolean> insert(@Valid @RequestBody CreatePostRequest request) {
        return Response.success(postService.createPost(request));
    }

    /**
     * 更新文章
     */
    @Operation(summary = "更新文章", description = "更新已有文章的内容和属性")
    @PutMapping
    public Response<Boolean> update(@Valid @RequestBody UpdatePostRequest request) {
        return Response.success(postService.updatePost(request));
    }

    /**
     * 删除文章（软删除）
     */
    @Operation(summary = "删除文章", description = "软删除文章，文章移至回收站")
    @DeleteMapping
    public Response<Boolean> delete(@RequestParam List<Long> idList) {
        return Response.success(postService.deletePosts(idList));
    }

    /**
     * 获取已删除的文章列表
     */
    @Operation(summary = "回收站文章列表", description = "查询已被软删除的文章列表")
    @GetMapping("/deleted")
    public Response<Page<PostListDTO>> getDeletedPosts(Page<Post> page, Post post) {
        return Response.success(postService.getDeletedPosts(page, post));
    }

    /**
     * 恢复已删除的文章
     */
    @Operation(summary = "恢复文章", description = "从回收站恢复已软删除的文章")
    @PostMapping("/restore")
    public Response<Boolean> restore(@RequestParam List<Long> idList) {
        return Response.success(postService.restorePosts(idList));
    }

    /**
     * 彻底删除文章（物理删除）
     */
    @Operation(summary = "彻底删除文章", description = "从数据库中物理删除文章，不可恢复")
    @DeleteMapping("/permanent")
    public Response<Boolean> permanentDelete(@RequestParam List<Long> idList) {
        return Response.success(postService.permanentDelete(idList));
    }

    /**
     * AI生成摘要
     */
    @Operation(summary = "AI生成摘要", description = "调用 AI 模型根据文章标题和内容生成摘要")
    @PostMapping("/summary/generate")
    public Response<String> generateSummary(@RequestBody SummaryRequest request) {
        String summary = aiSummaryService.generateSummary(
                request.getTitle(),
                request.getContent(),
                request.getMaxLength() != null ? request.getMaxLength() : 200
        );
        return Response.success(summary);
    }

    @Operation(summary = "总浏览量统计", description = "获取全站文章总浏览量")
    @GetMapping("/stats/views")
    public Response<Long> getTotalViewCount() {
        return Response.success(postService.getTotalViewCount());
    }

    /**
     * 获取热门文章列表
     */
    @Operation(summary = "热门文章", description = "获取浏览量最高的热门文章列表")
    @GetMapping("/popular")
    public Response<List<Post>> getPopularPosts(
            @RequestParam(defaultValue = "10") Integer size) {
        return Response.success(postService.getPopularPosts(size));
    }

    /**
     * 手动同步文章到指定平台
     */
    @Operation(summary = "同步文章到平台", description = "手动将文章同步到掘金、CSDN 等第三方平台")
    @PostMapping("/{id}/sync/{platform}")
    public Response<PlatformSyncResult> syncToPlatform(@PathVariable Long id, @PathVariable String platform) {
        Post post = postService.getByIdOrThrow(id);
        if (post.getStatus() != PostStatus.PUBLISHED) {
            return Response.errorT("只能同步已发布的文章");
        }
        PlatformSyncResult result = platformSyncOrchestrator.syncPostToPlatform(id, platform);
        return Response.success(result);
    }

    /**
     * 获取文章同步状态
     */
    @Operation(summary = "文章同步状态", description = "获取文章的第三方平台同步状态信息")
    @GetMapping("/{id}/sync-status")
    public Response<List<PostPlatformSync>> getSyncStatus(@PathVariable Long id) {
        return Response.success(postPlatformSyncService.getByPostId(id));
    }

    /**
     * 批量导入文章
     */
    @Operation(summary = "批量导入文章", description = "通过上传 JSON 文件批量导入文章")
    @PostMapping("/import")
    public Response<Map<String, Object>> importPosts(@RequestParam("file") MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        // 读取 JSON 文件内容
        byte[] bytes = file.getBytes();
        String jsonContent = new String(bytes, StandardCharsets.UTF_8);

        // 解析 JSON
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        List<ImportPostRequest> posts = objectMapper.readValue(jsonContent,
                new com.fasterxml.jackson.core.type.TypeReference<>() {
                });

        Long userId = 1L;
        Map<String, Object> result = postService.importPosts(posts, userId);
        return Response.success(result);
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
