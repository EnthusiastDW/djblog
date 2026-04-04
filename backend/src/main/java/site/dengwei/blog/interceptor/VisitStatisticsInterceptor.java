package site.dengwei.blog.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import site.dengwei.blog.service.VisitStatisticsService;
import site.dengwei.blog.util.RequestContextUtil;

/**
 * 访问统计拦截器
 *
 * @author dengwei
 * @since 2026-04-03
 */
@Component
@RequiredArgsConstructor
public class VisitStatisticsInterceptor implements HandlerInterceptor {

    private final VisitStatisticsService visitStatisticsService;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                Object handler, Exception ex) throws Exception {
        if (ex == null && response.getStatus() == 200) {
            String ip = RequestContextUtil.getClientIp(request);
            String userAgent = request.getHeader("User-Agent");
            visitStatisticsService.recordVisit(ip, userAgent);
        }
    }
}
