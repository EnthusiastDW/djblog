package site.dengwei.blog.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import site.dengwei.blog.entity.Category;
import site.dengwei.blog.entity.Tag;
import site.dengwei.blog.enums.PostStatus;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章列表 DTO，用于返回文章列表时携带分类和标签信息
 */
@Data
public class PostListDTO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 文章 ID
     */
    private Long id;
    
    /**
     * 文章标题
     */
    private String title;
    
    /**
     * 文章摘要
     */
    private String summary;
    
    /**
     * 文章别名
     */
    private String slug;
    
    /**
     * 封面图片地址
     */
    private String coverImage;
    
    /**
     * 文章状态
     */
    private PostStatus status;
    
    /**
     * 作者 ID
     */
    private Long authorId;
    
    /**
     * 分类 ID
     */
    private Long categoryId;
    
    /**
     * 分类名称（关联字段）
     */
    @TableField(exist = false)
    private String categoryName;
    
    /**
     * 标签列表（关联字段）
     */
    @TableField(exist = false)
    private List<Tag> tags;
    
    /**
     * 发布时间
     */
    private LocalDateTime publishedAt;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 浏览次数
     */
    private Long viewCount;
}
