package site.dengwei.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import site.dengwei.utils.validation.Slug;

/**
 * 创建分类请求DTO
 *
 * @author dengwei
 * @since 2025-09-08
 */
@Data
public class CreateCategoryRequest {
    
    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过50个字符")
    private String name;
    
    /**
     * 分类别名（可选）
     */
    @Slug
    @Size(max = 100, message = "分类别名长度不能超过100个字符")
    private String slug;
    
    /**
     * 分类描述
     */
    private String description;
    
    /**
     * 父级分类ID
     */
    private Long parentId;
}
