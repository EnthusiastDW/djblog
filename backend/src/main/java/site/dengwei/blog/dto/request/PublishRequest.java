package site.dengwei.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import site.dengwei.utils.validation.Slug;

/**
 * 发布文章请求DTO
 *
 * @author dengwei
 * @since 2025-09-08
 */
@Data
public class PublishRequest {
    
    /**
     * 文章ID（更新时传入）
     */
    private Long id;
    
    /**
     * 文章标题
     */
    @NotBlank(message = "文章标题不能为空")
    @Size(max = 200, message = "文章标题长度不能超过200个字符")
    private String title;
    
    /**
     * 文章内容
     */
    @NotBlank(message = "文章内容不能为空")
    private String content;
    
    /**
     * 文章别名（可选）
     */
    @Slug
    @Size(max = 200, message = "文章别名长度不能超过200个字符")
    private String slug;
    
    /**
     * 文章摘要
     */
    @Size(max = 500, message = "文章摘要长度不能超过500个字符")
    private String summary;
    
    /**
     * 封面图片地址
     */
    private String coverImage;
    
    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 标签ID列表
     */
    private Long[] tagIds;
}
