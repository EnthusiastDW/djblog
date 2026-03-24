package site.dengwei.blog.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 评论状态枚举
 *
 * @author dengwei
 * @since 2026-03-24
 */
@Getter
@RequiredArgsConstructor
public enum CommentStatus {

    /**
     * 待审核
     */
    PENDING("PENDING", "待审核"),

    /**
     * 已通过
     */
    APPROVED("APPROVED", "已通过"),

    /**
     * 已拒绝
     */
    REJECTED("REJECTED", "已拒绝");

    /**
     * 存储到数据库的值
     */
    @EnumValue
    @JsonValue
    private final String code;

    /**
     * 描述信息
     */
    private final String description;
}
