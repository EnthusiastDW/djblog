package site.dengwei.blog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import site.dengwei.blog.enums.PlatformSyncStatus;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文章平台同步记录实体类
 *
 * @author dengwei
 * @since 2026-05-29
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "post_platform_sync")
public class PostPlatformSync extends Model<PostPlatformSync> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 文章ID
     */
    private Long postId;

    /**
     * 平台代码
     */
    private String platformCode;

    /**
     * 同步状态
     */
    private PlatformSyncStatus syncStatus;

    /**
     * 外部平台文章URL
     */
    private String externalUrl;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 同步完成时间
     */
    private LocalDateTime syncedAt;

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
