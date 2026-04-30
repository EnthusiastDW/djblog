package site.dengwei.blog.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import site.dengwei.blog.service.VisitStatisticsService;
import site.dengwei.blog.util.RequestContextUtil;

/**
 * 全站访问统计拦截器
 * 用于统计整个网站的访问量（UV）
 *
 * @author dengwei
 * @since 2026-04-03
 */
@Component
@RequiredArgsConstructor
public class VisitStatisticsInterceptor implements HandlerInterceptor {

    private final VisitStatisticsService visitStatisticsService;

    @Override
    public void afterCompletion(HttpServletRequest request, @NonNull HttpServletResponse response,
                                @NonNull Object handler, Exception ex) {
        // 从请求头获取设备指纹ID
        String visitorId = request.getHeader("X-Visitor-ID");
        String ip = RequestContextUtil.getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        
        // 记录全站访问（优先使用 visitorId 去重）
        visitStatisticsService.recordVisit(visitorId, ip, userAgent);
    }
}
