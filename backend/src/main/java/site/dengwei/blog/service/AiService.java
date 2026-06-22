package site.dengwei.blog.service;

import java.util.Map;

/**
 * 统一 AI 服务接口
 * 整合 slug 生成、摘要生成、合并生成等所有 AI 调用
 *
 * @author dengwei
 * @since 2026-06-22
 */
public interface AiService {

    // ===== Slug 生成 =====

    /**
     * 根据标题生成 slug（最多重试3次）
     */
    String generateSlug(String title);

    /**
     * 根据标题生成 slug（指定重试次数）
     */
    String generateSlug(String title, int maxRetries);

    // ===== 摘要生成 =====

    /**
     * 根据文章标题和内容生成摘要（默认200字符）
     */
    String generateSummary(String title, String content);

    /**
     * 根据文章标题和内容生成摘要（指定最大长度）
     */
    String generateSummary(String title, String content, int maxLength);

    // ===== 合并生成 =====

    /**
     * 一次 AI 调用同时生成 slug 和摘要
     *
     * @return Map 包含 "slug" 和 "summary" 两个 key
     */
    Map<String, String> generateSlugAndSummary(String title, String content);
}
