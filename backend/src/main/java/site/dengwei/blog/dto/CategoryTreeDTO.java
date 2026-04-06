package site.dengwei.blog.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 树形分类 DTO
 */
@Data
public class CategoryTreeDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String description;
    private Long parentId;

    /**
     * 该分类自身的文章数
     */
    private Long postCount;

    /**
     * 含所有子分类的文章数（递归汇总）
     */
    private Long totalPostCount;

    /**
     * 子分类列表
     */
    private List<CategoryTreeDTO> children;
}
