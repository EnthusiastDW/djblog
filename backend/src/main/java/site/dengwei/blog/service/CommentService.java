package site.dengwei.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import site.dengwei.blog.dto.request.*;
import site.dengwei.blog.entity.Comment;

import java.util.List;

/**
 * 评论服务接口
 *
 * @author dengwei
 * @since 2025-09-08 11:56:31
 */
public interface CommentService extends IService<Comment> {

    /**
     * 根据ID查询评论，不存在则抛出异常
     * @param id 评论ID
     * @return 评论信息
     */
    Comment getByIdOrThrow(Long id);

    /**
     * 根据文章ID查询评论列表（树形结构）
     * @param postId 文章ID
     * @return 评论列表
     */
    List<Comment> getCommentsByPostId(Long postId);

    /**
     * 分页查询文章的评论
     * @param postId 文章ID
     * @param page 分页对象
     * @return 分页结果
     */
    IPage<Comment> getCommentsByPostId(Long postId, Page<Comment> page);

    /**
     * 发表评论
     * @param request 评论请求
     * @return 是否成功
     */
    boolean addComment(CreateCommentRequest request);

    /**
     * 审核评论
     * @param request 审核请求
     * @return 是否成功
     */
    boolean auditComment(AuditCommentRequest request);

    /**
     * 更新评论
     * @param request 更新请求
     * @return 是否成功
     */
    boolean updateComment(UpdateCommentRequest request);

    /**
     * 删除评论及其子评论
     * @param request 删除请求
     * @return 是否成功
     */
    boolean deleteComment(DeleteRequest request);

    /**
     * 获取待审核的评论列表
     * @param page 分页对象
     * @return 分页结果
     */
    IPage<Comment> getPendingComments(Page<Comment> page);
}