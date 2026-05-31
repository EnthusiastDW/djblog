package site.dengwei.blog.service.platform.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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

import java.util.List;

@Slf4j
@Service("juejinPublisher")
@RequiredArgsConstructor
public class JuejinPublisher extends AbstractPlatformPublisher {

    private final RestTemplate platformRestTemplate;
    private final ObjectMapper objectMapper;
    private final PlatformProperties platformProperties;

    @Override
    public String getPlatformCode() {
        return "JUEJIN";
    }

    @Override
    protected PlatformSyncResult doPublish(PlatformSyncRequest request) {
        log.info("同步文章到掘金: {}", request.getTitle());

        String accessToken = getAccessToken();
        if (accessToken == null) {
            return PlatformSyncResult.failed("JUEJIN", "环境变量 PLATFORM_JUEJIN_ACCESS_TOKEN 未配置");
        }

        String apiUrl = getApiUrl();
        if (apiUrl == null) {
            return PlatformSyncResult.failed("JUEJIN", "平台配置缺少 apiUrl");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Juejin-Token", accessToken);

            ObjectNode body = objectMapper.createObjectNode();
            body.put("title", request.getTitle());
            body.put("content", request.getContent());
            body.put("category_id", "2");

            ArrayNode tagsArray = objectMapper.createArrayNode();
            List<String> tags = request.getTags();
            if (tags != null) {
                for (String tag : tags) {
                    ObjectNode tagNode = objectMapper.createObjectNode();
                    tagNode.put("name", tag);
                    tagsArray.add(tagNode);
                }
            }
            body.set("tags", tagsArray);

            String requestBody = objectMapper.writeValueAsString(body);
            log.info("掘金 HTTP 请求体: POST {}\n{}", apiUrl, requestBody);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = platformRestTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode json = objectMapper.readTree(response.getBody());
                int errNo = json.get("err_no").asInt();
                if (errNo == 0) {
                    String articleId = json.path("data").path("article_id").asText();
                    String articleUrl = "https://juejin.cn/post/" + articleId;
                    log.info("掘金同步成功: {}", articleUrl);
                    return PlatformSyncResult.success("JUEJIN", articleUrl);
                } else {
                    String errMsg = json.path("err_msg").asText("未知错误");
                    log.warn("掘金同步失败: err_no={}, msg={}", errNo, errMsg);
                    return PlatformSyncResult.failed("JUEJIN", "掘金返回错误: " + errMsg);
                }
            } else {
                log.warn("掘金 API HTTP 异常: {}", response.getStatusCode());
                return PlatformSyncResult.failed("JUEJIN", "HTTP " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("掘金同步异常", e);
            return PlatformSyncResult.failed("JUEJIN", "请求异常: " + e.getMessage());
        }
    }

    @Override
    public boolean isEnabled() {
        return getAccessToken() != null;
    }

    @Override
    public boolean validateConfig() {
        return getAccessToken() != null;
    }

    private String getApiUrl() {
        PlatformProperties.Juejin juejin = platformProperties.juejin();
        return juejin != null ? juejin.apiUrl() : null;
    }

    private String getAccessToken() {
        PlatformProperties.Juejin juejin = platformProperties.juejin();
        return juejin != null ? juejin.accessToken() : null;
    }
}
