package site.dengwei.blog.service;

/**
 * AI Slug 生成服务接口
 * 使用 AI（千问模型）根据标题自动生成 URL 友好的 slug
 *
 * @author dengwei
 * @since 2026-03-24
 */
public interface AiSlugService {

    /**
     * 根据标题生成 slug
     * 支持中文和英文标题，自动转换为 URL 友好的英文 slug
     *
     * @param title 标题（中文或英文）
     * @return 生成的 slug
     */
    String generateSlug(String title);

    /**
     * 根据标题生成 slug（带重试机制）
     *
     * @param title 标题（中文或英文）
     * @param maxRetries 最大重试次数
     * @return 生成的 slug
     */
    String generateSlugWithRetry(String title, int maxRetries);
}
