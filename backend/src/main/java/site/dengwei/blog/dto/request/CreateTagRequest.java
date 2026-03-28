package site.dengwei.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建标签请求DTO
 *
 * @author dengwei
 * @since 2025-09-08
 */
@Data
public class CreateTagRequest {
    
    /**
     * 标签名称
     */
    @NotBlank(message = "标签名称不能为空")
    @Size(max = 50, message = "标签名称长度不能超过 50 个字符")
    private String name;
}
