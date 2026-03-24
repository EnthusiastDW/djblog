package site.dengwei.blog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.dengwei.blog.dto.AuthResponse;
import site.dengwei.blog.dto.LoginRequest;
import site.dengwei.blog.dto.RegisterRequest;
import site.dengwei.blog.entity.User;
import site.dengwei.blog.exception.BusinessException;
import site.dengwei.blog.service.UserService;
import site.dengwei.blog.util.JwtUtil;
import site.dengwei.common.beans.Response;

/**
 * 认证控制器，处理用户登录、登出和注册
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
     * 用户注册接口
     */
    @PostMapping("/register")
    public Response<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("收到用户注册请求: {}", registerRequest.getUsername());

        User newUser = userService.registerUser(registerRequest);
        String token = jwtUtil.generateToken(newUser.getUsername());

        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(token);
        authResponse.setUser(newUser);
        authResponse.setId(newUser.getId());

        log.info("用户注册成功: {}", registerRequest.getUsername());
        return Response.success(authResponse);
    }
}