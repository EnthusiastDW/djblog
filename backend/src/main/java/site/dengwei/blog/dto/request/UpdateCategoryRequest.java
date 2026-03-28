package site.dengwei.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新分类请求DTO
 *
 * @author dengwei
 * @since 2025-09-08
 */
@Data
public class UpdateCategoryRequest {
    
    /**
     * 分类ID
     */
    @NotNull(message = "分类ID不能为空")
    private Long id;
    
    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过 50 个字符")
    private String name;
        
    /**
     * 分类描述
     */
    private String description;
    
    /**
     * 父级分类ID
     */
    private Long parentId;
}
