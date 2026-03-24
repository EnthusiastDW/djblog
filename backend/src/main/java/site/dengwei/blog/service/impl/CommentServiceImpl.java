package site.dengwei.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dengwei.blog.dto.request.*;
import site.dengwei.blog.entity.Comment;
import site.dengwei.blog.enums.CommentStatus;
import site.dengwei.blog.exception.BusinessException;
import site.dengwei.blog.mapper.CommentMapper;
import site.dengwei.blog.service.CommentService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 评论服务实现类
 *
 * @author dengwei
 * @since 2025-09-08 11:56:31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Override
    public Comment getByIdOrThrow(Long id) {
        Comment comment = getById(id);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        return comment;
    }

    @Override
    public List<Comment> getCommentsByPostId(Long postId) {
        log.debug("查询文章评论列表，文章ID: {}", postId);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getPostId, postId)
               .eq(Comment::getStatus, CommentStatus.APPROVED)
               .orderByAsc(Comment::getCreatedAt);
        return this.list(wrapper);
    }

    @Override
    public IPage<Comment> getCommentsByPostId(Long postId, Page<Comment> page) {
        log.debug("分页查询文章评论，文章ID: {}, 页码: {}, 大小: {}", postId, page.getCurrent(), page.getSize());
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getPostId, postId)
               .eq(Comment::getStatus, CommentStatus.APPROVED)
               .orderByAsc(Comment::getCreatedAt);
        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addComment(CreateCommentRequest request) {
        log.info("添加评论，文章ID: {}", request.getPostId());

        Comment comment = new Comment();
        comment.setPostId(request.getPostId());
        comment.setParentId(request.getParentId());
        comment.setContent(request.getContent());
        comment.setAuthorName(request.getAuthorName());
        comment.setAuthorEmail(request.getAuthorEmail());
        comment.setAuthorUrl(request.getAuthorUrl());
        comment.setStatus(CommentStatus.PENDING);

        boolean result = this.save(comment);
        if (result) {
            log.info("评论添加成功，评论ID: {}", comment.getId());
        } else {
            log.error("评论添加失败");
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditComment(AuditCommentRequest request) {
        log.info("审核评论，评论ID: {}, 状态: {}", request.getId(), request.getStatus());

        Comment comment = getByIdOrThrow(request.getId());
        comment.setStatus(request.getStatus());
        
        boolean result = this.updateById(comment);
        if (result) {
            log.info("评论审核完成，评论ID: {}, 状态: {}", request.getId(), request.getStatus());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateComment(UpdateCommentRequest request) {
        log.info("更新评论，ID: {}", request.getId());
        
        Comment comment = getByIdOrThrow(request.getId());
        comment.setContent(request.getContent());
        
        return this.updateById(comment);
    }

    @Override
    public IPage<Comment> getPendingComments(Page<Comment> page) {
        log.debug("查询待审核评论列表");
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getStatus, CommentStatus.PENDING)
               .orderByDesc(Comment::getCreatedAt);
        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteComment(DeleteRequest request) {
        log.info("删除评论，ID列表: {}", request.getIdList());
        
        for (Long commentId : request.getIdList()) {
            Comment comment = getByIdOrThrow(commentId);
            deleteChildComments(commentId);
            this.removeById(commentId);
        }
        
        log.info("评论删除成功");
        return true;
    }

    /**
     * 递归删除子评论
     * @param parentId 父评论ID
     */
    private void deleteChildComments(Long parentId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getParentId, parentId);
        List<Comment> childComments = this.list(wrapper);

        if (!childComments.isEmpty()) {
            List<Long> childIds = childComments.stream()
                    .map(Comment::getId)
                    .collect(Collectors.toList());

            for (Long childId : childIds) {
                deleteChildComments(childId);
            }

            this.removeByIds(childIds);
            log.debug("删除子评论 {} 条，父评论ID: {}", childIds.size(), parentId);
        }
    }
}
