package site.dengwei.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 访问统计实体
 *
 * @author dengwei
 * @since 2026-04-03
 */
@Data
@TableName("visit_statistics")
public class VisitStatistics {

    /**
     * 访问统计 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 客户端 IP 地址
     */
    private String ip;

    /**
     * 访问日期
     */
    private LocalDate visitDate;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
