package site.dengwei.blog.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;
import site.dengwei.blog.service.AiSlugService;
import site.dengwei.utils.SlugUtils;

import java.util.Map;

/**
 * AI Slug 生成服务实现类
 * 使用阿里云千问模型根据标题生成 URL 友好的 slug
 *
 * @author dengwei
 * @since 2026-03-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiSlugServiceImpl implements AiSlugService {

    private final ChatClient.Builder chatClientBuilder;

    private static final String SLUG_GENERATION_PROMPT = """
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

    /**
     * 根据标题生成 slug
     * 支持中文和英文标题，自动转换为 URL 友好的英文 slug
     *
     * @param title 标题（中文或英文）
     * @return 生成的 slug
     */
    @Override
    public String generateSlug(String title) {
        return generateSlugWithRetry(title, 3);
    }

    /**
     * 根据标题生成 slug（带重试机制）
     *
     * @param title 标题（中文或英文）
     * @param maxRetries 最大重试次数
     * @return 生成的 slug
     */
    @Override
    public String generateSlugWithRetry(String title, int maxRetries) {
        if (StringUtils.isBlank(title)) {
            return "";
        }

        // 如果是纯英文，可以直接使用传统方法
        if (isPureEnglish(title)) {
            String slug = SlugUtils.generateSlug(title);
            if (SlugUtils.isValidSlug(slug)) {
                return slug;
            }
        }

        // 使用 AI 生成 slug
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                String slug = generateSlugByAi(title);
                if (SlugUtils.isValidSlug(slug)) {
                    log.info("AI generated slug for title '{}': {}", title, slug);
                    return slug;
                }
                log.warn("Invalid slug generated on attempt {}: {}", attempt, slug);
            } catch (Exception e) {
                log.error("Error generating slug on attempt {}: {}", attempt, e.getMessage());
            }
        }

        // AI 生成失败，使用传统方法作为降级
        log.warn("AI slug generation failed after {} attempts, using fallback method", maxRetries);
        return generateFallbackSlug(title);
    }

    /**
     * 使用 AI 生成 slug
     */
    private String generateSlugByAi(String title) {
        PromptTemplate promptTemplate = new PromptTemplate(SLUG_GENERATION_PROMPT);
        String prompt = promptTemplate.render(Map.of("title", title));

        ChatClient chatClient = chatClientBuilder.build();
        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        // 清理响应
        return cleanSlugResponse(response);
    }

    /**
     * 清理 AI 响应，提取 slug
     */
    private String cleanSlugResponse(String response) {
        if (StringUtils.isBlank(response)) {
            return "";
        }

        // 移除可能的 markdown 代码块标记
        String slug = response.trim()
                .replaceAll("```[a-z]*\\n?", "")
                .replaceAll("```", "")
                .trim();

        // 移除可能的引号
        slug = slug.replaceAll("^[\"']|[\"']$", "");

        // 只保留有效字符
        slug = slug.toLowerCase()
                .replaceAll("[^a-z0-9\\-_]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        // 限制长度
        if (slug.length() > 50) {
            slug = slug.substring(0, 50).replaceAll("-$", "");
        }

        return slug;
    }

    /**
     * 检查是否为纯英文
     */
    private boolean isPureEnglish(String text) {
        return text.matches("^[a-zA-Z0-9\\s\\-_]+$");
    }

    /**
     * 降级方法：使用拼音或简单转换
     */
    private String generateFallbackSlug(String title) {
        // 简单的降级策略：使用时间戳
        String timestamp = String.valueOf(System.currentTimeMillis());
        return "post-" + timestamp.substring(timestamp.length() - 8);
    }
}
