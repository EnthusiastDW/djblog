package site.dengwei.blog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import site.dengwei.blog.dto.AuthResponse;
import site.dengwei.blog.dto.LoginRequest;
import site.dengwei.blog.dto.RegisterRequest;
import site.dengwei.blog.entity.User;
import site.dengwei.blog.exception.BusinessException;
import site.dengwei.blog.service.UserService;
import site.dengwei.blog.util.JwtUtil;
import site.dengwei.common.beans.Response;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AuthController 测试类
 *
 * @author dengwei
 * @since 2025-09-08
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthController authController;

    private User testUser;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("new@example.com");
        registerRequest.setPassword("password123");
    }

    // ==================== 登录测试 ====================

    @Test
    @DisplayName("登录成功")
    void login_Success() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateToken("testuser")).thenReturn("test-token");
        when(userService.getOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            Response<AuthResponse> response = authController.login(loginRequest);

            // Then
            assertNotNull(response);
            assertEquals(200, response.getCode());
            assertNotNull(response.getData());
            assertEquals("test-token", response.getData().getToken());
            assertEquals("testuser", response.getData().getUser().getUsername());
        }
    }

    // ==================== 登出测试 ====================

    @Test
    @DisplayName("登出成功 - 已认证用户")
    void logout_WithAuthentication() {
        // Given
        when(authentication.getName()).thenReturn("testuser");

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);

            // When
            Response<String> response = authController.logout();

            // Then
            assertNotNull(response);
            assertEquals(200, response.getCode());
            assertEquals("登出成功", response.getData());
        }
    }

    @Test
    @DisplayName("登出成功 - 未认证用户")
    void logout_WithoutAuthentication() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null);

            // When
            Response<String> response = authController.logout();

            // Then
            assertNotNull(response);
            assertEquals(200, response.getCode());
            assertEquals("登出成功", response.getData());
        }
    }

    // ==================== 注册测试 ====================

    @Test
    @DisplayName("注册成功")
    void register_Success() {
        // Given
        when(userService.registerUser(any(RegisterRequest.class))).thenReturn(testUser);
        when(jwtUtil.generateToken("testuser")).thenReturn("test-token");

        // When
        Response<AuthResponse> response = authController.register(registerRequest);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals("test-token", response.getData().getToken());
    }

    @Test
    @DisplayName("注册失败 - 用户名已存在")
    void register_UsernameExists() {
        // Given - UserService会检查用户名重复
        when(userService.registerUser(any(RegisterRequest.class)))
                .thenThrow(new BusinessException("用户名已被占用"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> authController.register(registerRequest));
        assertEquals("用户名已被占用", exception.getMessage());
    }

    @Test
    @DisplayName("注册失败 - 邮箱已存在")
    void register_EmailExists() {
        // Given - UserService会检查邮箱重复
        when(userService.registerUser(any(RegisterRequest.class)))
                .thenThrow(new BusinessException("邮箱已被注册"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> authController.register(registerRequest));
        assertEquals("邮箱已被注册", exception.getMessage());
    }
}
