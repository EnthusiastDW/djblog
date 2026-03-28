package site.dengwei.blog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import site.dengwei.blog.enums.PostStatus;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文章实体类
 *
 * @author makejava
 * @since 2025-09-10 21:09:47
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "post", schema = "blog")
public class Post extends Model<Post> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 文章ID
     */
    private Long id;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 文章别名
     */
    private String slug;

    /**
     * 文章摘要
     */
    private String summary;

    /**
     * 封面图片地址
     */
    private String coverImage;

    /**
     * 文章状态
     */
    private PostStatus status;

    /**
     * 作者ID
     */
    private Long authorId;

    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 浏览次数
     */
    private Long viewCount;
    
    /**
     * 发布时间
     */
    private LocalDateTime publishedAt;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
