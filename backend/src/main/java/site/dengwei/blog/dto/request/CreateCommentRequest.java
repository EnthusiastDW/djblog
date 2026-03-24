package site.dengwei.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建评论请求DTO
 *
 * @author dengwei
 * @since 2025-09-08
 */
@Data
public class CreateCommentRequest {
    
    /**
     * 文章ID
     */
    @NotNull(message = "文章ID不能为空")
    private Long postId;
    
    /**
     * 父级评论ID（可选，用于回复）
     */
    private Long parentId;
    
    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 2000, message = "评论内容长度不能超过2000个字符")
    private String content;
    
    /**
     * 评论者姓名（未登录用户必填）
     */
    @Size(max = 50, message = "评论者姓名长度不能超过50个字符")
    private String authorName;
    
    /**
     * 评论者邮箱
     */
    @Size(max = 100, message = "评论者邮箱长度不能超过100个字符")
    private String authorEmail;
    
    /**
     * 评论者网址
     */
    @Size(max = 200, message = "评论者网址长度不能超过200个字符")
    private String authorUrl;
}
