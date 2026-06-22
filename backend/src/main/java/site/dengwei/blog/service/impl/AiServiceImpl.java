package site.dengwei.blog.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;
import site.dengwei.blog.service.AiService;
import site.dengwei.utils.SlugUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 统一 AI 服务实现
 * 整合 slug 生成、摘要生成、合并生成
 *
 * @author dengwei
 * @since 2026-06-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;

    // ==================== Prompt 常量 ====================

    private static final String SLUG_PROMPT = """
            You are a URL slug generator. Your task is to convert the given title into a URL-friendly slug.
            
            Rules:
            1. If the title is in Chinese, translate it to English first
            2. Convert to lowercase
            3. Replace spaces with hyphens (-)
            4. Remove special characters, keep only letters, numbers, and hyphens
            5. Make it concise and SEO-friendly
            6. Maximum 50 characters
            7. Output ONLY the slug, nothing else
            
            Title: {title}
            
            Slug:""";

    private static final String SUMMARY_PROMPT = """
            请为以下文章生成一个简洁的摘要。
            
            要求：
            1. 摘要长度不超过{maxLength}个字符
            2. 概括文章核心内容
            3. 语言简洁流畅
            4. 不要使用"本文"、"文章"等开头
            5. 直接输出摘要内容，不要添加任何其他说明
            6. 不要输出思考过程，只输出最终摘要结果
            
            标题：{title}
            
            内容：
            {content}
            
            摘要：""";

    private static final String COMBINED_PROMPT = """
            你是一个博客助手。给定文章标题和内容，请同时生成：
            1. slug：英文 URL 友好的短别名（全小写，连字符分隔，最多50字符）
            2. summary：中文摘要（不超过200字符，不要用"本文""文章"开头，直接概括核心内容）

            请严格按照以下 JSON 格式返回，不要包含其他任何内容：
            {"slug": "english-slug", "summary": "中文摘要内容"}

            标题：%s

            内容：
            %s""";

    private static final Pattern SLUG_FALLBACK_PATTERN =
            Pattern.compile("(?:slug|Slug)[：:\\s]*['\"]?([a-z0-9\\-]+)");

    // ==================== Slug 生成 ====================

    @Override
    public String generateSlug(String title) {
        return generateSlug(title, 3);
    }

    @Override
    public String generateSlug(String title, int maxRetries) {
        if (StringUtils.isBlank(title)) {
            return "";
        }

        // 纯英文可直接用传统方法
        if (isPureEnglish(title)) {
            String slug = SlugUtils.generateSlug(title);
            if (SlugUtils.isValidSlug(slug)) {
                return slug;
            }
        }

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                String slug = generateSlugByAi(title);
                if (SlugUtils.isValidSlug(slug)) {
                    log.info("AI generated slug for '{}': {}", title, slug);
                    return slug;
                }
                log.warn("Invalid slug on attempt {}: {}", attempt, slug);
            } catch (Exception e) {
                log.error("Slug generation failed on attempt {}: {}", attempt, e.getMessage());
            }
        }

        log.warn("AI slug generation failed after {} attempts, using fallback", maxRetries);
        return fallbackSlug();
    }

    private String generateSlugByAi(String title) {
        PromptTemplate promptTemplate = new PromptTemplate(SLUG_PROMPT);
        String prompt = promptTemplate.render(Map.of("title", title));

        ChatClient chatClient = chatClientBuilder.build();
        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        return cleanSlugResponse(response);
    }

    private String cleanSlugResponse(String response) {
        if (StringUtils.isBlank(response)) return "";

        String slug = response.trim()
                .replaceAll("```[a-z]*\\n?", "")
                .replaceAll("```", "")
                .replaceAll("^[\"']|[\"']$", "")
                .trim();

        slug = slug.toLowerCase()
                .replaceAll("[^a-z0-9\\-_]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        if (slug.length() > 50) {
            slug = slug.substring(0, 50).replaceAll("-$", "");
        }
        return slug;
    }

    private boolean isPureEnglish(String text) {
        return text.matches("^[a-zA-Z0-9\\s\\-_]+$");
    }

    private String fallbackSlug() {
        String ts = String.valueOf(System.currentTimeMillis());
        return "post-" + ts.substring(ts.length() - 8);
    }

    // ==================== 摘要生成 ====================

    @Override
    public String generateSummary(String title, String content) {
        return generateSummary(title, content, 200);
    }

    @Override
    public String generateSummary(String title, String content, int maxLength) {
        if (StringUtils.isBlank(content)) {
            return "";
        }

        try {
            String truncatedContent = content.length() > 2000
                    ? content.substring(0, 2000) + "..."
                    : content;

            String prompt = SUMMARY_PROMPT
                    .replace("{title}", StringUtils.defaultString(title, "无标题"))
                    .replace("{content}", truncatedContent)
                    .replace("{maxLength}", String.valueOf(maxLength));

            ChatClient chatClient = chatClientBuilder.build();
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.info("AI summary raw: {}", response);
            String summary = cleanSummaryResponse(response, maxLength);
            log.info("AI summary result: {}", summary);
            return summary;
        } catch (Exception e) {
            log.error("Summary generation failed: {}", e.getMessage());
            return fallbackSummary(content, maxLength);
        }
    }

    private String cleanSummaryResponse(String response, int maxLength) {
        if (StringUtils.isBlank(response)) return "";

        String summary = response.trim()
                .replaceAll("<think>.*?</think>", "")
                .replaceAll("```.*?```", "")
                .replaceAll("摘要[：:]", "")
                .trim();

        if (summary.length() > maxLength) {
            summary = summary.substring(0, maxLength);
        }
        return summary;
    }

    private String fallbackSummary(String content, int maxLength) {
        if (StringUtils.isBlank(content)) return "";

        String plainText = content
                .replaceAll("#+\\s*", "")
                .replaceAll("\\*+", "")
                .replaceAll("`+", "")
                .replaceAll("\\[([^]]+)]\\([^)]+\\)", "$1")
                .replaceAll("\\n+", " ")
                .trim();

        if (plainText.length() <= maxLength) {
            return plainText;
        }
        return plainText.substring(0, maxLength) + "...";
    }

    // ==================== 合并生成 ====================

    @Override
    public Map<String, String> generateSlugAndSummary(String title, String content) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("标题不能为空");
        }

        String truncatedContent = (content != null && content.length() > 2000)
                ? content.substring(0, 2000) + "..."
                : (content != null ? content : "");

        String prompt = String.format(COMBINED_PROMPT, title, truncatedContent);

        ChatClient chatClient = chatClientBuilder.build();
        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        log.info("AI combined raw: {}", response);
        Map<String, String> result = parseCombinedResponse(response);
        log.info("AI combined result - slug: {}, summary: {}", result.get("slug"), result.get("summary"));
        return result;
    }

    private Map<String, String> parseCombinedResponse(String response) {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("slug", "");
        result.put("summary", "");

        if (response == null || response.isBlank()) return result;

        String cleaned = response.trim()
                .replaceAll("```[a-z]*\\n?", "")
                .replaceAll("```", "")
                .replaceAll("<think>.*?</think>", "")
                .trim();

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> parsed = objectMapper.readValue(cleaned, Map.class);

            result.put("slug", sanitizeSlug(parsed.get("slug")));
            result.put("summary", sanitizeSummary(parsed.get("summary")));
        } catch (Exception e) {
            log.warn("Failed to parse AI JSON, trying fallback: {}", e.getMessage());
            Matcher m = SLUG_FALLBACK_PATTERN.matcher(cleaned);
            if (m.find()) {
                result.put("slug", m.group(1));
            }
        }

        return result;
    }

    private String sanitizeSlug(Object raw) {
        if (raw == null) return "";
        String slug = raw.toString().trim().toLowerCase()
                .replaceAll("[^a-z0-9\\-_]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        if (slug.length() > 50) slug = slug.substring(0, 50).replaceAll("-$", "");
        return slug;
    }

    private String sanitizeSummary(Object raw) {
        if (raw == null) return "";
        String s = raw.toString().trim();
        if (s.length() > 200) s = s.substring(0, 200);
        return s;
    }
}
