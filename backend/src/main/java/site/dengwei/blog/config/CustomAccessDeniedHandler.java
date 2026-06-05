package site.dengwei.blog.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import site.dengwei.blog.dto.Response;

import java.io.IOException;

/**
 * 请添加注释
 *
 * @author deng_wei_2011@126.com
 * @since 2026-06-05 12:03
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.warn("未授权访问被拒绝: {}", request.getRequestURI());
        // 设置响应内容类型和状态码
        response.setContentType("application/json;charset=UTF-8");
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");

        // 创建人性化错误信息
        Response<Void> errorResponse = new Response<>(
                HttpStatus.FORBIDDEN.value(),
                "您没有该操作的权限，请联系管理员",
                null
        );

        // 将错误信息写入响应
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
