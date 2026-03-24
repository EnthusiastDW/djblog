package site.dengwei.blog.exception;

import lombok.Getter;

/**
 * 自定义业务异常类
 *
 * @author dengwei
 * @since 2025-12-08
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private final Integer code;
    
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }
    
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

}