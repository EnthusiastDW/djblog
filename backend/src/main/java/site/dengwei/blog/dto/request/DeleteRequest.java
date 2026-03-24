package site.dengwei.blog.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量删除请求DTO
 *
 * @author dengwei
 * @since 2025-09-08
 */
@Data
public class DeleteRequest {
    
    /**
     * ID列表
     */
    @NotEmpty(message = "请选择要删除的项")
    private List<Long> idList;
}
