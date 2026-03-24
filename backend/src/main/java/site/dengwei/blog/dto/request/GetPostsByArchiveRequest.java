package site.dengwei.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 根据归档查询文章请求DTO
 *
 * @author dengwei
 * @since 2025-09-08
 */
@Data
public class GetPostsByArchiveRequest {
    
    /**
     * 年月格式：yyyy-MM
     */
    @NotBlank(message = "年月不能为空")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "年月格式不正确，应为 yyyy-MM")
    private String yearMonth;
}
