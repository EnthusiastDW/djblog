package site.dengwei.blog.dto;

import lombok.Data;
import site.dengwei.blog.entity.User;

import java.io.Serial;
import java.io.Serializable;

/**
 * 认证响应数据传输对象
 *
 * @author dengwei
 * @since 2025-12-08
 */
@Data
public class AuthResponse implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * JWT token
     */
    private String token;
    
    /**
     * token类型
     */
    private String tokenType = "Bearer";
    
    /**
     * 用户名
     */
    private User user;
    
    /**
     * 用户ID
     */
    private Long id;
}