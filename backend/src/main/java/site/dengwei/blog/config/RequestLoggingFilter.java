package site.dengwei.blog.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import site.dengwei.blog.util.RequestContextUtil;

import java.io.IOException;

/**
 * Request logging filter for caching request body.
 * Wraps HttpServletRequest with ContentCachingRequestWrapper to allow multiple reads of request body.
 *
 * @author dengwei
 * @since 2026-04-03
 */
@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    public static final String CACHED_REQUEST_ATTRIBUTE = "cachedRequest";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        request.setAttribute(CACHED_REQUEST_ATTRIBUTE, wrappedRequest);

        long startTime = System.currentTimeMillis();
        filterChain.doFilter(wrappedRequest, response);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Request: IP: {} - {} {} - Status: {} - Duration: {}ms",
                RequestContextUtil.getClientIp(request),
                request.getMethod(), request.getRequestURI(),
                response.getStatus(), duration);
    }
}