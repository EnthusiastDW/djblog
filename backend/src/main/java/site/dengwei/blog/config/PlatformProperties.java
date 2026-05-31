package site.dengwei.blog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 平台发布凭据配置（从环境变量读取，避免明文存储）
 *
 * <p>环境变量命名规则：
 * <ul>
 *   <li>掘金: {@code PLATFORM_JUEJIN_ACCESS_TOKEN}</li>
 *   <li>CSDN: {@code PLATFORM_CSDN_ACCESS_TOKEN}</li>
 *   <li>博客园: {@code PLATFORM_CNBLOG_PERSONAL_TOKEN}</li>
 *   <li>思否: {@code PLATFORM_SEGMENTFAULT_ACCESS_TOKEN}</li>
 * </ul>
 *
 * @author dengwei
 * @since 2026-05-31
 */
@ConfigurationProperties(prefix = "platform")
public record PlatformProperties(
        Juejin juejin,
        Csdn csdn,
        Cnblog cnblog,
        Segmentfault segmentfault
) {

    /**
     * 掘金配置
     *
     * @param apiUrl       API 端点地址
     * @param accessToken  个人访问令牌
     */
    public record Juejin(String apiUrl, String accessToken) {}

    /**
     * CSDN 配置
     *
     * @param apiUrl       API 端点地址
     * @param accessToken  访问令牌
     */
    public record Csdn(String apiUrl, String accessToken) {}

    /**
     * 博客园配置
     *
     * @param apiUrl         API 端点地址
     * @param personalToken  个人访问令牌
     */
    public record Cnblog(String apiUrl, String personalToken) {}

    /**
     * 思否配置
     *
     * @param apiUrl       API 端点地址
     * @param accessToken  访问令牌
     */
    public record Segmentfault(String apiUrl, String accessToken) {}
}
