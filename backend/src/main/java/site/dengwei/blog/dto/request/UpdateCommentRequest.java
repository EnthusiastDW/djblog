package site.dengwei.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新评论请求DTO
 *
 * @author dengwei
 * @since 2025-09-08
 */
@Data
public class UpdateCommentRequest {
    
    /**
     * 评论ID
     */
    @NotNull(message = "评论ID不能为空")
    private Long id;
    
    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 2000, message = "评论内容长度不能超过2000个字符")
    private String content;
}
