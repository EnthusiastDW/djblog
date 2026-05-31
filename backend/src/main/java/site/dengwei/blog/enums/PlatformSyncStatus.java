package site.dengwei.blog.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 平台同步状态枚举
 *
 * @author dengwei
 * @since 2026-05-29
 */
@Getter
@RequiredArgsConstructor
public enum PlatformSyncStatus {

    /**
     * 待同步
     */
    PENDING("PENDING", "待同步"),

    /**
     * 同步中
     */
    SYNCING("SYNCING", "同步中"),

    /**
     * 同步成功
     */
    SUCCESS("SUCCESS", "同步成功"),

    /**
     * 同步失败
     */
    FAILED("FAILED", "同步失败");

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
