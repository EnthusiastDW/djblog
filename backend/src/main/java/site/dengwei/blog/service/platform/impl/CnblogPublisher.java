package site.dengwei.blog.service.platform.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import site.dengwei.blog.config.PlatformProperties;
import site.dengwei.blog.service.platform.AbstractPlatformPublisher;
import site.dengwei.blog.service.platform.dto.PlatformSyncRequest;
import site.dengwei.blog.service.platform.dto.PlatformSyncResult;

@Slf4j
@Service("cnblogPublisher")
@RequiredArgsConstructor
public class CnblogPublisher extends AbstractPlatformPublisher {

    private final RestTemplate platformRestTemplate;
    private final ObjectMapper objectMapper;
    private final PlatformProperties platformProperties;

    @Override
    public String getPlatformCode() {
        return "CNBLOG";
    }

    @Override
    protected PlatformSyncResult doPublish(PlatformSyncRequest request) {
        log.info("同步文章到博客园: {}", request.getTitle());

        String personalToken = getPersonalToken();
        if (personalToken == null) {
            return PlatformSyncResult.failed("CNBLOG", "环境变量 PLATFORM_CNBLOG_PERSONAL_TOKEN 未配置");
        }

        String apiUrl = getApiUrl();
        if (apiUrl == null) {
            return PlatformSyncResult.failed("CNBLOG", "平台配置缺少 apiUrl");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(personalToken);
            headers.set("Authorization-Type", "pat");

            ObjectNode body = objectMapper.createObjectNode();
            body.put("title", request.getTitle());
            body.put("body", request.getContent());
            body.put("postType", "BlogPost");
            body.put("postFormat", "Markdown");
            if (request.getSummary() != null && !request.getSummary().isEmpty()) {
                body.put("description", request.getSummary());
            }
            if (request.getSlug() != null && !request.getSlug().isEmpty()) {
                body.put("slug", request.getSlug());
            }
            body.put("isPublished", true);
            body.put("isAllowComments", true);

            String requestBody = objectMapper.writeValueAsString(body);
            log.debug("博客园 HTTP 请求体: POST {}\n{}", apiUrl, requestBody);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = platformRestTemplate.exchange(
                    apiUrl, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode json = objectMapper.readTree(response.getBody());
                boolean success = json.path("success").asBoolean(false);

                if (success) {
                    String postUrl = json.path("value").path("postUrl").asText();
                    log.info("博客园同步成功: {}", postUrl);
                    return PlatformSyncResult.success("CNBLOG", postUrl);
                } else {
                    String errMsg = json.path("message").asText("未知错误");
                    log.warn("博客园同步失败: {}", errMsg);
                    return PlatformSyncResult.failed("CNBLOG", "博客园返回错误: " + errMsg);
                }
            } else {
                log.warn("博客园 API HTTP 异常: {}", response.getStatusCode());
                return PlatformSyncResult.failed("CNBLOG", "HTTP " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("博客园同步异常", e);
            return PlatformSyncResult.failed("CNBLOG", "请求异常: " + e.getMessage());
        }
    }

    @Override
    public boolean isEnabled() {
        return getPersonalToken() != null;
    }

    @Override
    public boolean validateConfig() {
        return getPersonalToken() != null;
    }

    private String getApiUrl() {
        PlatformProperties.Cnblog cnblog = platformProperties.cnblog();
        return cnblog != null ? cnblog.apiUrl() : null;
    }

    private String getPersonalToken() {
        PlatformProperties.Cnblog cnblog = platformProperties.cnblog();
        return cnblog != null ? cnblog.personalToken() : null;
    }
}
