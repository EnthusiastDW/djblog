package site.dengwei.blog.exception;

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

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * @author dengwei
 * @since 2025-12-08
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     *
     * @param e 业务异常
     * @return 响应结果
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常：{}", e.getMessage());
        return Response.error(e.getMessage());
    }

    /**
     * 处理参数校验异常（RequestBody）
     *
     * @param e 参数校验异常
     * @return 响应结果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response<Void> handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验异常：{}", errorMessage);
        return Response.error(errorMessage);
    }

    /**
     * 处理参数绑定异常（非 RequestBody）
     *
     * @param e 参数绑定异常
     * @return 响应结果
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response<Void> handleBindException(BindException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数绑定异常：{}", errorMessage);
        return Response.error(errorMessage);
    }

    /**
     * 处理认证异常
     *
     * @param e 认证异常
     * @return 响应结果
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Response<Void> handleAuthenticationException(AuthenticationException e) {
        log.warn("认证异常：{}", e.getMessage());
        return Response.error("认证失败：" + e.getMessage());
    }

    /**
     * 处理凭证错误异常
     *
     * @param e 凭证错误异常
     * @return 响应结果
     */
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Response<Void> handleBadCredentialsException(BadCredentialsException e) {
        log.warn("凭证错误：{}", e.getMessage());
        return Response.error("用户名或密码错误");
    }

    /**
     * 处理权限不足异常
     *
     * @param e 权限不足异常
     * @return 响应结果
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Response<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("访问被拒绝：{}", e.getMessage());
        return Response.error("没有访问权限");
    }

    /**
     * 处理其他所有异常
     *
     * @param e 异常
     * @return 响应结果
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Response<Void> handleException(Exception e) {
        log.error("系统内部异常：", e);
        return Response.error("系统内部错误，请稍后重试");
    }
}
