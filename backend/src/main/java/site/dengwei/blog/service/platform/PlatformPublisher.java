package site.dengwei.blog.service.platform;

import site.dengwei.blog.service.platform.dto.PlatformSyncRequest;
import site.dengwei.blog.service.platform.dto.PlatformSyncResult;

public interface PlatformPublisher {

    String getPlatformCode();

    PlatformSyncResult publish(PlatformSyncRequest request);

    boolean isEnabled();

    boolean validateConfig();
}
