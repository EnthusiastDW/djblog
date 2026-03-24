package site.dengwei.blog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.dengwei.blog.dto.request.*;
import site.dengwei.blog.entity.User;
import site.dengwei.blog.exception.BusinessException;
import site.dengwei.blog.service.UserService;
import site.dengwei.common.beans.Response;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UserController 测试类
 *
 * @author dengwei
 * @since 2025-09-08
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encoded_password");
    }

    // ==================== 查询测试 ====================

    @Test
    @DisplayName("分页查询所有用户")
    void selectAll_Success() {
        // Given
        Page<User> page = new Page<>(1, 10);
        Page<User> resultPage = new Page<>(1, 10);
        resultPage.setRecords(Arrays.asList(testUser));
        when(userService.page(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(resultPage);

        // When
        Response<Page<User>> response = userController.selectAll(page, new User());

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals(1, response.getData().getRecords().size());
    }

    @Test
    @DisplayName("根据ID查询用户")
    void selectOne_Success() {
        // Given
        when(userService.getByIdOrThrow(1L)).thenReturn(testUser);

        // When
        Response<User> response = userController.selectOne(1L);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("testuser", response.getData().getUsername());
    }

    @Test
    @DisplayName("根据ID查询用户不存在")
    void selectOne_NotFound() {
        // Given
        when(userService.getByIdOrThrow(999L)).thenThrow(new BusinessException("用户不存在"));

        // When & Then
        assertThrows(BusinessException.class, () -> userController.selectOne(999L));
    }

    // ==================== 创建测试 ====================

    @Test
    @DisplayName("创建用户成功")
    void insert_Success() {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("newuser");
        request.setEmail("new@example.com");
        request.setPassword("password123");
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(true);

        // When
        Response<Boolean> response = userController.insert(request);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertTrue(response.getData());
        verify(userService).createUser(any(CreateUserRequest.class));
    }

    // ==================== 更新测试 ====================

    @Test
    @DisplayName("更新用户成功")
    void update_Success() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest();
        request.setId(1L);
        request.setEmail("updated@example.com");
        when(userService.updateUser(any(UpdateUserRequest.class))).thenReturn(true);

        // When
        Response<Boolean> response = userController.update(request);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertTrue(response.getData());
        verify(userService).updateUser(any(UpdateUserRequest.class));
    }

    // ==================== 删除测试 ====================

    @Test
    @DisplayName("删除用户成功")
    void delete_Success() {
        // Given
        DeleteRequest request = new DeleteRequest();
        request.setIdList(Arrays.asList(1L));
        when(userService.deleteUsers(any(DeleteRequest.class))).thenReturn(true);

        // When
        Response<Boolean> response = userController.delete(request);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertTrue(response.getData());
        verify(userService).deleteUsers(any(DeleteRequest.class));
    }
}
