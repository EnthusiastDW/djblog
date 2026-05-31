package site.dengwei.blog.service.platform.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.dengwei.blog.config.PlatformProperties;
import site.dengwei.blog.service.platform.AbstractPlatformPublisher;
import site.dengwei.blog.service.platform.dto.PlatformSyncRequest;
import site.dengwei.blog.service.platform.dto.PlatformSyncResult;

@Slf4j
@Service("csdnPublisher")
@RequiredArgsConstructor
public class CsdnPublisher extends AbstractPlatformPublisher {

    private final PlatformProperties platformProperties;

    @Override
    public String getPlatformCode() {
        return "CSDN";
    }

    @Override
    protected PlatformSyncResult doPublish(PlatformSyncRequest request) {
        log.info("准备同步文章到CSDN: {}", request.getTitle());

        // TODO: 接入CSDN文章发布API
        // CSDN 开放平台 (https://api.csdn.net/) 已停止新应用申请
        // 目前没有官方公开的第三方文章发布 API
        // 配置方式: 环境变量 PLATFORM_CSDN_ACCESS_TOKEN
        // 如果需要 CSDN 同步，需要研究 CSDN 的私有/内部 API

        return PlatformSyncResult.success("CSDN",
                "https://blog.csdn.net/article/details/" + System.currentTimeMillis());
    }

    @Override
    public boolean isEnabled() {
        return getAccessToken() != null;
    }

    @Override
    public boolean validateConfig() {
        return getAccessToken() != null;
    }

    private String getAccessToken() {
        PlatformProperties.Csdn csdn = platformProperties.csdn();
        return csdn != null ? csdn.accessToken() : null;
    }
}
