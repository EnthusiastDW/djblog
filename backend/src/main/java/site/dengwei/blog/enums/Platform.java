package site.dengwei.blog.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 平台枚举
 *
 * @author dengwei
 * @since 2026-05-29
 */
@Getter
@RequiredArgsConstructor
public enum Platform {

    /**
     * 掘金
     */
    JUEJIN("JUEJIN", "掘金"),

    /**
     * CSDN
     */
    CSDN("CSDN", "CSDN"),

    /**
     * 博客园
     */
    CNBLOG("CNBLOG", "博客园"),

    /**
     * 思否
     */
    SEGMENTFAULT("SEGMENTFAULT", "思否");

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
