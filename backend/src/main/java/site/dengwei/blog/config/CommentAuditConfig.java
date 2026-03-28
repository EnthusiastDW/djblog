package site.dengwei.blog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 评论审核配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "blog.comment")
public class CommentAuditConfig {
    
    /**
     * 是否启用评论审核
     * true: 需要审核，false: 默认通过
     */
    private Boolean auditEnabled = false;
    
    /**
     * 是否启用 AI 智能判断
     * true: 使用 AI 判断是否需要审核，false: 不使用 AI
     */
    private Boolean aiEnabled = false;
    
    /**
     * AI 判断模式
     * AUTO: AI 自动判断直接通过或拒绝
     * MANUAL: AI 标记可疑评论，仍需人工审核
     */
    private AiMode aiMode = AiMode.MANUAL;
    
    public enum AiMode {
        /**
         * AI 自动决定，无需人工审核
         */
        AUTO,
        
        /**
         * AI 辅助判断，仍需人工审核
         */
        MANUAL
    }
}
