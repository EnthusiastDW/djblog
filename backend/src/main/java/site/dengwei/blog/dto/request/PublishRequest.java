package site.dengwei.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 发布文章请求DTO
 *
 * @author dengwei
 * @since 2025-09-08
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PublishRequest extends BasePostRequest {
    
    /**
     * 文章内容（发布时必填）
     */
    @NotBlank(message = "文章内容不能为空")
    @Override
    public String getContent() {
        return super.getContent();
    }
}
