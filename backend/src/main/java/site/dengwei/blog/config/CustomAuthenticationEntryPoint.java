package site.dengwei.blog.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import site.dengwei.common.beans.Response;

import java.io.IOException;

/**
 * 自定义认证入口点
 * 当未经身份验证的用户试图访问受保护的资源时，返回人性化的JSON响应
 */
@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        log.warn("未授权访问被拒绝: {}", request.getRequestURI());
                         
        // 设置响应内容类型和状态码
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        // 创建人性化错误信息
        Response<Void> errorResponse = new Response<>(
                HttpStatus.UNAUTHORIZED.value(),
                "访问被拒绝：请先登录获取访问权限",
                null
        );

        // 将错误信息写入响应
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}