package site.dengwei.blog.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 通用ID请求DTO
 *
 * @author dengwei
 * @since 2025-09-08
 */
@Data
public class GetByIdRequest {
    
    /**
     * ID
     */
    @NotNull(message = "ID不能为空")
    private Long id;
}
