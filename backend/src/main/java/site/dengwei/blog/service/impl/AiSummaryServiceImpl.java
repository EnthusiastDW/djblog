package site.dengwei.blog.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import site.dengwei.blog.service.AiSummaryService;

/**
 * AI 摘要生成服务实现类
 *
 * @author dengwei
 * @since 2026-03-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiSummaryServiceImpl implements AiSummaryService {

    private final ChatClient.Builder chatClientBuilder;

    private static final String SUMMARY_PROMPT = """
            请为以下文章生成一个简洁的摘要。
            
            要求：
            1. 摘要长度不超过{maxLength}个字符
            2. 概括文章核心内容
            3. 语言简洁流畅
            4. 不要使用"本文"、"文章"等开头
            5. 直接输出摘要内容，不要添加任何其他说明
            
            标题：{title}
            
            内容：
            {content}
            
            摘要：""";

    @Override
    public String generateSummary(String content, int maxLength) {
        return generateSummary("", content, maxLength);
    }

    @Override
    public String generateSummary(String title, String content, int maxLength) {
        if (StringUtils.isBlank(content)) {
            return "";
        }

        try {
            // 截取内容前2000字符以避免token过多
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

            String summary = cleanSummary(response, maxLength);
            log.info("AI generated summary: {}", summary);
            return summary;
        } catch (Exception e) {
            log.error("Error generating summary: {}", e.getMessage());
            // 降级：返回内容前maxLength个字符
            return generateFallbackSummary(content, maxLength);
        }
    }

    /**
     * 清理摘要响应
     */
    private String cleanSummary(String response, int maxLength) {
        if (StringUtils.isBlank(response)) {
            return "";
        }

        String summary = response.trim()
                .replaceAll("```.*?```", "") // 移除代码块
                .replaceAll("摘要[：:]", "") // 移除"摘要："前缀
                .trim();

        // 限制长度
        if (summary.length() > maxLength) {
            summary = summary.substring(0, maxLength);
        }

        return summary;
    }

    /**
     * 降级方法：截取内容前部分
     */
    private String generateFallbackSummary(String content, int maxLength) {
        if (StringUtils.isBlank(content)) {
            return "";
        }

        // 移除markdown标记
        String plainText = content
                .replaceAll("#+\\s*", "")
                .replaceAll("\\*+", "")
                .replaceAll("`+", "")
                .replaceAll("\\[([^\\]]+)\\]\\([^)]+\\)", "$1")
                .replaceAll("\\n+", " ")
                .trim();

        if (plainText.length() <= maxLength) {
            return plainText;
        }

        return plainText.substring(0, maxLength) + "...";
    }
}
