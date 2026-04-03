package site.dengwei.blog.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.util.ContentCachingRequestWrapper;
import site.dengwei.blog.config.RequestLoggingFilter;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Request context utility for extracting request information.
 *
 * @author dengwei
 * @since 2026-04-03
 */
public class RequestContextUtil {

    private static final int MAX_PAYLOAD_LENGTH = 2048;

    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "password", "pwd", "pass", "secret", "token", "key", "apiKey", "api_key", "accessToken"
    );

    private static final Pattern SENSITIVE_PATTERN = SENSITIVE_FIELDS.stream()
            .map(field -> "(?i)" + field + "=([^&]*)")
            .reduce((p1, p2) -> p1 + "|" + p2)
            .map(Pattern::compile)
            .orElse(null);

    /**
     * Get client IP address from request.
     *
     * @param request HTTP servlet request
     * @return client IP address
     */
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * Get request parameters as a sanitized string.
     *
     * @param request HTTP servlet request
     * @return sanitized parameters string
     */
    public static String getRequestParams(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap == null || parameterMap.isEmpty()) {
            return "";
        }
        Map<String, String> sanitizedParams = new HashMap<>();
        parameterMap.forEach((key, values) -> {
            if (values != null && values.length > 0) {
                sanitizedParams.put(key, sanitizeParams(values[0]));
            }
        });
        return sanitizedParams.toString();
    }

    /**
     * Sanitize sensitive parameters like passwords.
     *
     * @param value parameter value
     * @return sanitized value
     */
    public static String sanitizeParams(String value) {
        if (value == null) {
            return "";
        }
        if (SENSITIVE_PATTERN != null) {
            return SENSITIVE_PATTERN.matcher(value).replaceAll("$1=******");
        }
        return value;
    }

    /**
     * Get request body from cached request.
     *
     * @param request HTTP servlet request
     * @return request body string (up to 2KB), or empty string if not available
     */
    public static String getRequestBody(HttpServletRequest request) {
        try {
            Object cachedRequest = request.getAttribute(RequestLoggingFilter.CACHED_REQUEST_ATTRIBUTE);
            if (cachedRequest instanceof ContentCachingRequestWrapper) {
                ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) cachedRequest;
                byte[] content = wrapper.getContentAsByteArray();
                if (content.length > 0) {
                    String body = new String(content, StandardCharsets.UTF_8);
                    if (body.length() > MAX_PAYLOAD_LENGTH) {
                        return body.substring(0, MAX_PAYLOAD_LENGTH) + "... [truncated]";
                    }
                    return sanitizeParams(body);
                }
            }
        } catch (Exception e) {
            // Ignore exceptions when reading request body
        }
        return "";
    }
}