package site.dengwei.blog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import site.dengwei.blog.enums.CommentStatus;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论实体类
 *
 * @author makejava
 * @since 2025-09-10 21:09:48
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "comment", schema = "blog")
public class Comment extends Model<Comment> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 评论ID
     */
    private Long id;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论者姓名
     */
    private String authorName;

    /**
     * 评论者邮箱
     */
    private String authorEmail;

    /**
     * 评论者网址
     */
    private String authorUrl;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 文章ID
     */
    private Long postId;

    /**
     * 父级评论ID
     */
    private Long parentId;
    
    /**
     * 评论状态
     */
    private CommentStatus status;
    
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
    
    /**
     * 回复列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<Comment> replies;
}
