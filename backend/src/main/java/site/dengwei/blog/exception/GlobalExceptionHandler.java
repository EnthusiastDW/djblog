package site.dengwei.blog.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.dengwei.blog.dto.Response;
import site.dengwei.blog.util.RequestContextUtil;

import java.util.stream.Collectors;

/**
 * Global exception handler for unified error handling.
 *
 * @author dengwei
 * @since 2025-12-08
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== 业务异常 ====================
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.error("业务异常 | IP: {} | URI: {} | Method: {} | Params: {} | Body: {} | Error: {}",
                RequestContextUtil.getClientIp(request), request.getRequestURI(),
                request.getMethod(), RequestContextUtil.getRequestParams(request),
                RequestContextUtil.getRequestBody(request), e.getMessage(), e);
        return Response.error(e.getMessage());
    }

    // ==================== 参数校验异常 ====================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response<Void> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.error("参数校验异常 | IP: {} | URI: {} | Method: {} | Params: {} | Body: {} | Error: {}",
                RequestContextUtil.getClientIp(request), request.getRequestURI(),
                request.getMethod(), RequestContextUtil.getRequestParams(request),
                RequestContextUtil.getRequestBody(request), errorMessage, e);
        return Response.error(errorMessage);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response<Void> handleBindException(BindException e, HttpServletRequest request) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.error("参数绑定异常 | IP: {} | URI: {} | Method: {} | Params: {} | Body: {} | Error: {}",
                RequestContextUtil.getClientIp(request), request.getRequestURI(),
                request.getMethod(), RequestContextUtil.getRequestParams(request),
                RequestContextUtil.getRequestBody(request), errorMessage, e);
        return Response.error(errorMessage);
    }

    // ==================== 认证或凭证异常 ====================
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Response<Void> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        log.error("认证异常 | IP: {} | URI: {} | Method: {} | Params: {} | Body: {} | Error: {}",
                RequestContextUtil.getClientIp(request), request.getRequestURI(),
                request.getMethod(), RequestContextUtil.getRequestParams(request),
                RequestContextUtil.getRequestBody(request), e.getMessage(), e);
        return Response.error("认证失败：" + e.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Response<Void> handleBadCredentialsException(BadCredentialsException e, HttpServletRequest request) {
        log.error("凭证错误 | IP: {} | URI: {} | Method: {} | Params: {} | Body: {} | Error: {}",
                RequestContextUtil.getClientIp(request), request.getRequestURI(),
                request.getMethod(), RequestContextUtil.getRequestParams(request),
                RequestContextUtil.getRequestBody(request), e.getMessage(), e);
        return Response.error("用户名或密码错误");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Response<Void> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.error("访问拒绝 | IP: {} | URI: {} | Method: {} | Params: {} | Body: {} | Error: {}",
                RequestContextUtil.getClientIp(request), request.getRequestURI(),
                request.getMethod(), RequestContextUtil.getRequestParams(request),
                RequestContextUtil.getRequestBody(request), e.getMessage(), e);
        return Response.error("没有访问权限");
    }

    // ==================== 兜底异常 ====================
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Response<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常 | IP: {} | URI: {} | Method: {} | Params: {} | Body: {} | Error: {}",
                RequestContextUtil.getClientIp(request), request.getRequestURI(),
                request.getMethod(), RequestContextUtil.getRequestParams(request),
                RequestContextUtil.getRequestBody(request), e.getMessage(), e);
        return Response.error("系统内部错误，请稍后重试");
    }
}
