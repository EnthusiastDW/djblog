package site.dengwei.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新标签请求DTO
 *
 * @author dengwei
 * @since 2025-09-08
 */
@Data
public class UpdateTagRequest {
    
    /**
     * 标签ID
     */
    @NotNull(message = "标签ID不能为空")
    private Long id;
    
    /**
     * 标签名称
     */
    @NotBlank(message = "标签名称不能为空")
    @Size(max = 50, message = "标签名称长度不能超过 50 个字符")
    private String name;
}
