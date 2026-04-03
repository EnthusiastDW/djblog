package site.dengwei.blog.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import site.dengwei.blog.entity.RequestLog;
import site.dengwei.blog.service.RequestLogService;
import site.dengwei.blog.util.RequestContextUtil;

/**
 * Request log interceptor for capturing request details.
 *
 * @author dengwei
 * @since 2026-04-03
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RequestLogInterceptor implements HandlerInterceptor {

    private static final String START_TIME = "startTime";
    private static final ThreadLocal<RequestLog> REQUEST_LOG_HOLDER = new ThreadLocal<>();

    private final RequestLogService requestLogService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME, startTime);

        RequestLog requestLog = new RequestLog();
        requestLog.setIp(RequestContextUtil.getClientIp(request));
        requestLog.setMethod(request.getMethod());
        requestLog.setUri(request.getRequestURI());
        requestLog.setParams(RequestContextUtil.getRequestParams(request));
        requestLog.setUserAgent(request.getHeader("User-Agent"));

        REQUEST_LOG_HOLDER.set(requestLog);
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME);
        RequestLog requestLog = REQUEST_LOG_HOLDER.get();

        try {
            if (startTime != null && requestLog != null) {
                long duration = System.currentTimeMillis() - startTime;
                requestLog.setStatusCode(response.getStatus());
                requestLog.setDuration(duration);
                requestLog.setRequestBody(RequestContextUtil.getRequestBody(request));

                if (ex != null) {
                    requestLog.setErrorMessage(ex.getMessage());
                }
                requestLogService.saveLog(requestLog);
            }
        } finally {
            REQUEST_LOG_HOLDER.remove();
        }
    }

}