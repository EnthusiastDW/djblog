package site.dengwei.blog.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新关于我内容请求
 *
 * @author dengwei
 */
@Data
public class UpdateAboutRequest {

    /**
     * 关于我内容（Markdown格式）
     */
    @Size(max = 50000, message = "关于我内容长度不能超过50000个字符")
    private String aboutContent;
}
