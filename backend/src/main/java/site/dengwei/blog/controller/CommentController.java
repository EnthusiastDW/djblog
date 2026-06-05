package site.dengwei.blog.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import site.dengwei.blog.dto.request.*;
import site.dengwei.blog.entity.Comment;
import site.dengwei.blog.service.CommentService;
import site.dengwei.blog.util.LambdaQueryUtils;
import site.dengwei.blog.dto.Response;

import java.util.List;

/**
 * 评论控制器
 *
 * @author dengwei
 * @since 2025-09-08 11:56:31
 */
@Tag(name = "Comment", description = "评论管理 — 发表、审核、嵌套回复、垃圾评论标记")
@Slf4j
@RestController
@RequestMapping("comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 分页查询所有数据
     */
    @Operation(summary = "评论列表（后台）", description = "分页查询所有评论记录（管理后台使用）")
    @GetMapping
    public Response<IPage<Comment>> selectAll(Page<Comment> page, Comment comment) {
        return Response.success(commentService.page(page, LambdaQueryUtils.buildFromEntity(comment)));
    }

    /**
     * 根据ID查询评论
     */
    @Operation(summary = "评论详情", description = "根据评论ID获取评论信息")
    @GetMapping("{id}")
    public Response<Comment> selectOne(@PathVariable Long id) {
        return Response.success(commentService.getByIdOrThrow(id));
    }

    /**
     * 根据文章ID查询评论列表
     */
    @Operation(summary = "文章评论列表", description = "根据文章ID获取该文章的所有评论（前台展示）")
    @GetMapping("/post/{postId}")
    public Response<List<Comment>> getCommentsByPostId(@PathVariable Long postId) {
        return Response.success(commentService.getCommentsByPostId(postId));
    }

    /**
     * 根据文章ID分页查询评论
     */
    @Operation(summary = "文章评论分页", description = "根据文章ID分页查询评论，支持查看更多加载")
    @GetMapping("/post/{postId}/page")
    public Response<IPage<Comment>> getCommentsByPostIdPage(
            @PathVariable Long postId,
            Page<Comment> page) {
        return Response.success(commentService.getCommentsByPostId(postId, page));
    }

    /**
     * 发表评论
     */
    @Operation(summary = "发表评论", description = "用户或游客发表评论，支持嵌套回复")
    @PostMapping
    public Response<Boolean> insert(@Valid @RequestBody CreateCommentRequest request) {
        return Response.success(commentService.addComment(request));
    }

    /**
     * 审核评论
     */
    @Operation(summary = "审核评论", description = "管理员审核评论，通过或驳回")
    @PutMapping("/audit")
    public Response<Boolean> auditComment(@Valid @RequestBody AuditCommentRequest request) {
        return Response.success(commentService.auditComment(request));
    }

    /**
     * 获取待审核评论列表
     */
    @Operation(summary = "待审核评论列表", description = "获取所有待审核的评论（管理后台使用）")
    @GetMapping("/pending")
    public Response<IPage<Comment>> getPendingComments(Page<Comment> page) {
        return Response.success(commentService.getPendingComments(page));
    }

    /**
     * 更新评论
     */
    @Operation(summary = "更新评论", description = "更新评论内容（管理员可用）")
    @PutMapping
    public Response<Boolean> update(@Valid @RequestBody UpdateCommentRequest request) {
        return Response.success(commentService.updateComment(request));
    }

    /**
     * 删除评论
     */
    @Operation(summary = "删除评论", description = "删除指定评论（管理员可用）")
    @DeleteMapping
    public Response<Boolean> delete(@Valid @RequestBody DeleteRequest request) {
        return Response.success(commentService.deleteComment(request));
    }
}
