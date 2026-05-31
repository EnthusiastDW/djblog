package site.dengwei.blog.service.platform.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.dengwei.blog.config.PlatformProperties;
import site.dengwei.blog.service.platform.AbstractPlatformPublisher;
import site.dengwei.blog.service.platform.dto.PlatformSyncRequest;
import site.dengwei.blog.service.platform.dto.PlatformSyncResult;

@Slf4j
@Service("segmentfaultPublisher")
@RequiredArgsConstructor
public class SfPublisher extends AbstractPlatformPublisher {

    private final PlatformProperties platformProperties;

    @Override
    public String getPlatformCode() {
        return "SEGMENTFAULT";
    }

    @Override
    protected PlatformSyncResult doPublish(PlatformSyncRequest request) {
        log.info("准备同步文章到思否: {}", request.getTitle());

        // TODO: 接入思否 (SegmentFault) 文章发布 API
        // 思否社区目前没有公开的第三方文章发布 API
        // 配置方式: 环境变量 PLATFORM_SEGMENTFAULT_ACCESS_TOKEN

        return PlatformSyncResult.success("SEGMENTFAULT",
                "https://segmentfault.com/a/" + System.currentTimeMillis());
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
        PlatformProperties.Segmentfault segmentfault = platformProperties.segmentfault();
        return segmentfault != null ? segmentfault.accessToken() : null;
    }
}
