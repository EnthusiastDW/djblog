package site.dengwei.blog.service.platform;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import site.dengwei.blog.service.platform.dto.PlatformSyncRequest;
import site.dengwei.blog.service.platform.dto.PlatformSyncResult;

@Slf4j
public abstract class AbstractPlatformPublisher implements PlatformPublisher {

    @Value("${blog.site-domain:https://blog.dengwei.site}")
    private String siteDomain;

    @Override
    public PlatformSyncResult publish(PlatformSyncRequest request) {
        String contentWithLink = appendOriginalLink(request.getContent(), request.getSlug());
        request.setContent(contentWithLink);
        return doPublish(request);
    }

    protected abstract PlatformSyncResult doPublish(PlatformSyncRequest request);

    protected String appendOriginalLink(String content, String slug) {
        if (slug == null || slug.isEmpty()) {
            return content;
        }
        String originalUrl = siteDomain + "/article/" + slug;
        return content + "\n\n---\n原文链接：" + originalUrl;
    }
}
