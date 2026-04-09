package site.dengwei.blog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import site.dengwei.blog.config.InitialPasswordHolder;
import site.dengwei.blog.dto.AuthResponse;
import site.dengwei.blog.dto.LoginRequest;
import site.dengwei.blog.dto.RegisterRequest;
import site.dengwei.blog.entity.User;
import site.dengwei.blog.exception.BusinessException;
import site.dengwei.blog.service.UserService;
import site.dengwei.blog.util.JwtUtil;
import site.dengwei.blog.dto.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 *
 * @author dengwei
 * @since 2025/9/7 10:10
 */
@Slf4j
@RequestMapping("/auth")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 用户登录接口
     *
     * @param loginRequest 登录请求
     * @return 认证响应
     */
    @PostMapping("/login")
    public Response<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("用户尝试登录: {}", loginRequest.getUsername());
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtUtil.generateToken(loginRequest.getUsername());
        log.info("用户登录成功: {}", loginRequest.getUsername());

        User user = userService.getOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, loginRequest.getUsername())
        );

        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(token);
        authResponse.setUser(user);

        return Response.success(authResponse);
    }

    /**
     * 用户登出接口
     *
     * @return 操作结果
     */
    @PostMapping("/logout")
    public Response<String> logout() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            log.info("用户登出: {}", auth.getName());
        }
        return Response.success("登出成功");
    }

    /**
     * 用户注册接口（仅允许无用户时注册）
     *
     * @param registerRequest 注册请求
     * @return 认证响应
     */
    @PostMapping("/register")
    public Response<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("收到用户注册请求: {}", registerRequest.getUsername());

        // 检查是否已有用户
        long userCount = userService.count();
        if (userCount > 0) {
            log.warn("注册失败：系统已有用户，不允许再次注册");
            throw new BusinessException("系统已存在用户，无法继续注册");
        }

        User newUser = userService.registerUser(registerRequest);
        String token = jwtUtil.generateToken(newUser.getUsername());

        // 清除初始密码
        InitialPasswordHolder.clear();

        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(token);
        authResponse.setUser(newUser);
        authResponse.setId(newUser.getId());

        log.info("用户注册成功: {}", registerRequest.getUsername());
        return Response.success(authResponse);
    }

    /**
     * 检查系统是否有用户
     *
     * @return 系统状态信息
     */
    @GetMapping("/has-user")
    public Response<Map<String, Object>> hasUser() {
        long userCount = userService.count();
        Map<String, Object> result = new HashMap<>();
        result.put("hasUser", userCount > 0);
        result.put("needsInitialSetup", userCount == 0);
        return Response.success(result);
    }

    /**
     * 验证初始密码（用于首次登录前的验证）
     *
     * @param request 包含初始密码的请求
     * @return 验证结果
     */
    @PostMapping("/verify-initial-password")
    public Response<Map<String, Object>> verifyInitialPassword(@RequestBody Map<String, String> request) {
        String password = request.get("password");
        
        if (password == null || password.isEmpty()) {
            throw new BusinessException("密码不能为空");
        }

        // 检查是否已有用户
        long userCount = userService.count();
        if (userCount > 0) {
            throw new BusinessException("系统已有用户，无需初始密码验证");
        }

        // 验证初始密码
        String initialPassword = InitialPasswordHolder.getPassword();
        if (initialPassword == null || !initialPassword.equals(password)) {
            throw new BusinessException("初始密码错误");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("valid", true);
        result.put("message", "验证成功，请完成注册");
        return Response.success(result);
    }
}