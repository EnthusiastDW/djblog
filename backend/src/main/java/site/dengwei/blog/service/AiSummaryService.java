package site.dengwei.blog.service;

/**
 * AI 摘要生成服务接口
 *
 * @author dengwei
 * @since 2026-03-25
 */
public interface AiSummaryService {

    /**
     * 根据文章内容生成摘要
     *
     * @param content 文章内容
     * @param maxLength 摘要最大长度
     * @return 生成的摘要
     */
    String generateSummary(String content, int maxLength);

    /**
     * 根据文章标题和内容生成摘要
     *
     * @param title 文章标题
     * @param content 文章内容
     * @param maxLength 摘要最大长度
     * @return 生成的摘要
     */
    String generateSummary(String title, String content, int maxLength);
}
