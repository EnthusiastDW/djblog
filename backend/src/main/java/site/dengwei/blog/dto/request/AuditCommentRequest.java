package site.dengwei.blog.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import site.dengwei.blog.enums.CommentStatus;

/**
 * 审核评论请求DTO
 *
 * @author dengwei
 * @since 2025-09-08
 */
@Data
public class AuditCommentRequest {
    
    /**
     * 评论ID
     */
    @NotNull(message = "评论ID不能为空")
    private Long id;
    
    /**
     * 审核状态
     */
    @NotNull(message = "审核状态不能为空")
    private CommentStatus status;
}
