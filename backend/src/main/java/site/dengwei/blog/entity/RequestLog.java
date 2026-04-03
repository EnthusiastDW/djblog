package site.dengwei.blog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Request log entity for tracking HTTP requests.
 *
 * @author dengwei
 * @since 2026-04-03
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "request_log", schema = "blog")
public class RequestLog extends Model<RequestLog> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Log ID
     */
    private Long id;

    /**
     * Client IP address
     */
    private String ip;

    /**
     * Request method
     */
    private String method;

    /**
     * Request URI
     */
    private String uri;

    /**
     * Request parameters
     */
    private String params;

    /**
     * Request body (only first 2KB)
     */
    private String requestBody;

    /**
     * User agent
     */
    private String userAgent;

    /**
     * Response status code
     */
    private Integer statusCode;

    /**
     * Error message
     */
    private String errorMessage;

    /**
     * Request duration in milliseconds
     */
    private Long duration;

    /**
     * Create time
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
