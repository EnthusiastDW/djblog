package site.dengwei.blog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.dengwei.blog.dto.request.*;
import site.dengwei.blog.entity.Comment;
import site.dengwei.blog.enums.CommentStatus;
import site.dengwei.blog.exception.BusinessException;
import site.dengwei.blog.service.CommentService;
import site.dengwei.common.beans.Response;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * CommentController 测试类
 *
 * @author dengwei
 * @since 2025-09-08
 */
@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    private Comment testComment;

    @BeforeEach
    void setUp() {
        testComment = new Comment();
        testComment.setId(1L);
        testComment.setPostId(1L);
        testComment.setContent("测试评论");
        testComment.setAuthorName("测试用户");
        testComment.setStatus(CommentStatus.APPROVED);
    }

    // ==================== 查询测试 ====================

    @Test
    @DisplayName("分页查询所有评论")
    void selectAll_Success() {
        // Given
        Page<Comment> page = new Page<>(1, 10);
        Page<Comment> resultPage = new Page<>(1, 10);
        resultPage.setRecords(Arrays.asList(testComment));
        when(commentService.page(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(resultPage);

        // When
        Response<IPage<Comment>> response = commentController.selectAll(page, new Comment());

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals(1, response.getData().getRecords().size());
    }

    @Test
    @DisplayName("根据ID查询评论")
    void selectOne_Success() {
        // Given
        when(commentService.getByIdOrThrow(1L)).thenReturn(testComment);

        // When
        Response<Comment> response = commentController.selectOne(1L);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("测试评论", response.getData().getContent());
    }

    @Test
    @DisplayName("根据文章ID查询评论列表")
    void getCommentsByPostId_Success() {
        // Given
        when(commentService.getCommentsByPostId(1L)).thenReturn(Arrays.asList(testComment));

        // When
        Response<List<Comment>> response = commentController.getCommentsByPostId(1L);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals(1, response.getData().size());
    }

    // ==================== 创建测试 ====================

    @Test
    @DisplayName("发表评论成功")
    void insert_Success() {
        // Given
        CreateCommentRequest request = new CreateCommentRequest();
        request.setPostId(1L);
        request.setContent("新评论");
        request.setAuthorName("新用户");
        when(commentService.addComment(any(CreateCommentRequest.class))).thenReturn(true);

        // When
        Response<Boolean> response = commentController.insert(request);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertTrue(response.getData());
    }

    // ==================== 审核测试 ====================

    @Test
    @DisplayName("审核评论成功")
    void auditComment_Success() {
        // Given
        AuditCommentRequest request = new AuditCommentRequest();
        request.setId(1L);
        request.setStatus(CommentStatus.APPROVED);
        when(commentService.auditComment(any(AuditCommentRequest.class))).thenReturn(true);

        // When
        Response<Boolean> response = commentController.auditComment(request);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertTrue(response.getData());
    }

    // ==================== 更新测试 ====================

    @Test
    @DisplayName("更新评论成功")
    void update_Success() {
        // Given
        UpdateCommentRequest request = new UpdateCommentRequest();
        request.setId(1L);
        request.setContent("更新后的评论");
        when(commentService.updateComment(any(UpdateCommentRequest.class))).thenReturn(true);

        // When
        Response<Boolean> response = commentController.update(request);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertTrue(response.getData());
    }

    // ==================== 删除测试 ====================

    @Test
    @DisplayName("删除评论成功")
    void delete_Success() {
        // Given
        DeleteRequest request = new DeleteRequest();
        request.setIdList(Arrays.asList(1L));
        when(commentService.deleteComment(any(DeleteRequest.class))).thenReturn(true);

        // When
        Response<Boolean> response = commentController.delete(request);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertTrue(response.getData());
    }
}
