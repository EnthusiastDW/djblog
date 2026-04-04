package site.dengwei.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 导入文章请求
 *
 * @author dengwei
 * @since 2026-04-03
 */
@Data
public class ImportPostRequest {

    /**
     * 文章标题
     */
    @NotBlank(message = "标题不能为空")
    private String title;

    /**
     * 文章别名
     */
    private String slug;

    /**
     * 分类名称（自动创建）
     */
    @NotBlank(message = "分类不能为空")
    private String category;

    /**
     * 标签列表（自动创建，最多 3 个）
     */
    private List<String> tags;

    /**
     * 文章摘要
     */
    private String summary;

    /**
     * 文章内容
     */
    @NotBlank(message = "内容不能为空")
    private String content;

    /**
     * 封面图片地址
     */
    private String coverImage;

    /**
     * 创建时间
     */
    private String createdAt;

    /**
     * 更新时间
     */
    private String updatedAt;

    /**
     * 文章状态
     */
    private String status;
}
