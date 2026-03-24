package site.dengwei.blog.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import site.dengwei.blog.dto.request.*;
import site.dengwei.blog.entity.Comment;
import site.dengwei.blog.service.CommentService;
import site.dengwei.blog.util.LambdaQueryUtils;
import site.dengwei.common.beans.Response;

import java.util.List;

/**
 * 评论控制器
 *
 * @author dengwei
 * @since 2025-09-08 11:56:31
 */
@Slf4j
@RestController
@RequestMapping("comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 分页查询所有数据
     */
    @GetMapping
    public Response<IPage<Comment>> selectAll(IPage<Comment> page, Comment comment) {
        return Response.success(commentService.page(page, LambdaQueryUtils.buildFromEntity(comment)));
    }

    /**
     * 根据ID查询评论
     */
    @GetMapping("{id}")
    public Response<Comment> selectOne(@PathVariable Long id) {
        return Response.success(commentService.getByIdOrThrow(id));
    }

    /**
     * 根据文章ID查询评论列表
     */
    @GetMapping("/post/{postId}")
    public Response<List<Comment>> getCommentsByPostId(@PathVariable Long postId) {
        return Response.success(commentService.getCommentsByPostId(postId));
    }

    /**
     * 根据文章ID分页查询评论
     */
    @GetMapping("/post/{postId}/page")
    public Response<IPage<Comment>> getCommentsByPostIdPage(
            @PathVariable Long postId,
            Page<Comment> page) {
        return Response.success(commentService.getCommentsByPostId(postId, page));
    }

    /**
     * 发表评论
     */
    @PostMapping
    public Response<Boolean> insert(@Valid @RequestBody CreateCommentRequest request) {
        return Response.success(commentService.addComment(request));
    }

    /**
     * 审核评论
     */
    @PutMapping("/audit")
    public Response<Boolean> auditComment(@Valid @RequestBody AuditCommentRequest request) {
        return Response.success(commentService.auditComment(request));
    }

    /**
     * 获取待审核评论列表
     */
    @GetMapping("/pending")
    public Response<IPage<Comment>> getPendingComments(Page<Comment> page) {
        return Response.success(commentService.getPendingComments(page));
    }

    /**
     * 更新评论
     */
    @PutMapping
    public Response<Boolean> update(@Valid @RequestBody UpdateCommentRequest request) {
        return Response.success(commentService.updateComment(request));
    }

    /**
     * 删除评论
     */
    @DeleteMapping
    public Response<Boolean> delete(@Valid @RequestBody DeleteRequest request) {
        return Response.success(commentService.deleteComment(request));
    }
}
