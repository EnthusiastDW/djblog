package site.dengwei.blog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 标签实体类
 *
 * @author makejava
 * @since 2025-09-10 21:09:47
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "tag", schema = "blog")
public class Tag extends Model<Tag> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 标签ID
     */
    private Long id;
    
    /**
     * 标签名称
     */
    @NotBlank(message = "标签名称不能为空")
    @Size(max = 50, message = "标签名称长度不能超过 50 个字符")
    private String name;
        
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}