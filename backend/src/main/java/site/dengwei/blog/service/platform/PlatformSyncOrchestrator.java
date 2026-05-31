package site.dengwei.blog.service.platform;

import site.dengwei.blog.service.platform.dto.PlatformSyncResult;

public interface PlatformSyncOrchestrator {

    void syncPost(Long postId, String[] platforms);

    PlatformSyncResult syncPostToPlatform(Long postId, String platformCode);
}
