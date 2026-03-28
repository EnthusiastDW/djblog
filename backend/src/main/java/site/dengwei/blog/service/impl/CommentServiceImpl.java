package site.dengwei.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dengwei.blog.config.CommentAuditConfig;
import site.dengwei.blog.dto.request.*;
import site.dengwei.blog.entity.Comment;
import site.dengwei.blog.entity.User;
import site.dengwei.blog.enums.CommentStatus;
import site.dengwei.blog.exception.BusinessException;
import site.dengwei.blog.mapper.CommentMapper;
import site.dengwei.blog.service.CommentAiAuditService;
import site.dengwei.blog.service.CommentService;
import site.dengwei.blog.service.UserService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private final UserService userService;
    private final CommentAuditConfig auditConfig;
    private final CommentAiAuditService aiAuditService;

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
        // 先获取所有评论
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getPostId, postId)
               .eq(Comment::getStatus, CommentStatus.APPROVED)
               .orderByAsc(Comment::getCreatedAt);
        List<Comment> allComments = this.list(wrapper);
        
        // 构建评论树
        Map<Long, Comment> commentMap = new HashMap<>();
        List<Comment> rootComments = new ArrayList<>();
        
        // 先将所有评论放入map
        for (Comment comment : allComments) {
            comment.setReplies(new ArrayList<>());
            commentMap.put(comment.getId(), comment);
        }
        
        // 构建评论树
        for (Comment comment : allComments) {
            if (comment.getParentId() == null) {
                // 根评论
                rootComments.add(comment);
            } else {
                // 回复
                Comment parentComment = commentMap.get(comment.getParentId());
                if (parentComment != null) {
                    parentComment.getReplies().add(comment);
                }
            }
        }
        
        return rootComments;
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
        log.info("添加评论，文章 ID: {}", request.getPostId());
    
        // 如果是登录用户，自动填充用户信息
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!"anonymousUser".equals(username)) {
            User user = userService.findByUsername(username);
            if (user != null) {
                request.setUserId(user.getId());
                // 如果前端没有传入作者名称，使用用户的用户名
                if (request.getAuthorName() == null || request.getAuthorName().isEmpty()) {
                    request.setAuthorName(user.getUsername());
                }
                // 如果前端没有传入作者邮箱，使用用户的邮箱
                if (request.getAuthorEmail() == null || request.getAuthorEmail().isEmpty()) {
                    request.setAuthorEmail(user.getEmail());
                }
            }
        }
    
        Comment comment = new Comment();
        comment.setPostId(request.getPostId());
        comment.setParentId(request.getParentId());
        comment.setContent(request.getContent());
        comment.setAuthorName(request.getAuthorName());
        comment.setAuthorEmail(request.getAuthorEmail());
        comment.setAuthorUrl(request.getAuthorUrl());
        comment.setUserId(request.getUserId());
        
        // 根据配置决定评论状态
        CommentStatus status = determineCommentStatus(comment);
        comment.setStatus(status);
    
        boolean result = this.save(comment);
        if (result) {
            log.info("评论添加成功，评论 ID: {}, 状态：{}", comment.getId(), status);
        } else {
            log.error("评论添加失败");
        }
        return result;
    }
    
    /**
     * 根据配置决定评论状态
     * @param comment 评论内容
     * @return 评论状态
     */
    private CommentStatus determineCommentStatus(Comment comment) {
        // 如果未启用审核，直接通过
        if (Boolean.FALSE.equals(auditConfig.getAuditEnabled())) {
            log.info("未启用评论审核，直接通过");
            return CommentStatus.APPROVED;
        }
        
        // 如果启用了 AI 判断，使用 AI 判断
        if (Boolean.TRUE.equals(auditConfig.getAiEnabled())) {
            return aiAuditService.judgeCommentStatus(comment);
        }
        
        // 默认情况：需要审核但未启用 AI，设置为待审核
        log.info("评论需要审核，设置为待审核状态");
        return CommentStatus.PENDING;
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
