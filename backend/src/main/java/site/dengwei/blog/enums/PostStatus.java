package site.dengwei.blog.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 文章状态枚举
 *
 * @author dengwei
 * @since 2026-03-24
 */
@Getter
@RequiredArgsConstructor
public enum PostStatus {

    /**
     * 草稿
     */
    DRAFT("DRAFT", "草稿"),

    /**
     * 已发布
     */
    PUBLISHED("PUBLISHED", "已发布"),

    /**
     * 已下架
     */
    UNPUBLISHED("UNPUBLISHED", "已下架");

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
