package site.dengwei.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 文章访问统计实体
 *
 * @author dengwei
 * @since 2026-04-30
 */
@Data
@TableName("post_visit_statistics")
public class PostVisitStatistics {

    /**
     * 访问统计 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文章 ID
     */
    private Long postId;

    /**
     * 访客设备指纹ID
     */
    private String visitorId;

    /**
     * 访问日期
     */
    private LocalDate visitDate;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
