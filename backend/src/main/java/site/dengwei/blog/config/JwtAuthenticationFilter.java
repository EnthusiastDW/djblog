package site.dengwei.blog.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import site.dengwei.blog.util.JwtUtil;

import java.io.IOException;

/**
 * JWT认证过滤器
 * 用于验证请求中的JWT token并设置Spring Security上下文
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        // JWT Token格式为 "Bearer token"
        if (StringUtils.hasText(requestTokenHeader) && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtUtil.getUsernameFromToken(jwtToken);
            } catch (IllegalArgumentException e) {
                log.error("无法获取JWT Token");
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "无效的Token");
                return;
            } catch (ExpiredJwtException e) {
                log.warn("JWT Token已过期");
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token已过期，请重新登录");
                return;
            }
        }

        // 验证token
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            if (Boolean.TRUE.equals(jwtUtil.validateToken(jwtToken, username))) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                // token验证失败
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token验证失败，请重新登录");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status);
        
        ObjectMapper objectMapper = new ObjectMapper();
        site.dengwei.blog.dto.Response<Void> errorResponse = new site.dengwei.blog.dto.Response<>(
                status,
                message,
                null
        );
        
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}