package site.dengwei.blog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import site.dengwei.blog.config.CommentAuditConfig;
import site.dengwei.blog.entity.Comment;
import site.dengwei.blog.enums.CommentStatus;

/**
 * 评论 AI 审核服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentAiAuditService {
    
    private final CommentAuditConfig auditConfig;
    private final ChatClient.Builder chatClientBuilder;
    
    private static final String AUDIT_PROMPT = """
            你是一个评论审核助手。请判断以下评论是否合适。
            
            判断标准：
            1. 如果评论包含广告、推销、垃圾信息，返回 "REJECT"
            2. 如果评论包含敏感词汇、违规内容，返回 "REJECT"  
            3. 如果评论是正常的内容，返回 "APPROVE"
            
            评论内容：{content}
            
            请直接返回 "APPROVE" 或 "REJECT"，不要返回其他内容。""";
    
    /**
     * 使用 AI 判断评论状态
     * @param comment 评论内容
     * @return 评论状态
     */
    public CommentStatus judgeCommentStatus(Comment comment) {
        if (!auditConfig.getAiEnabled()) {
            // 未启用 AI，返回待审核
            return CommentStatus.PENDING;
        }
        
        log.info("使用 AI 判断评论状态，评论内容：{}", comment.getContent());
        
        try {
            String result = auditCommentByAi(comment.getContent());
            
            if ("APPROVE".equalsIgnoreCase(result)) {
                if (auditConfig.getAiMode() == CommentAuditConfig.AiMode.AUTO) {
                    log.info("AI 自动模式，评论正常，直接通过");
                    return CommentStatus.APPROVED;
                }
                log.info("AI 辅助模式，标记为待审核，等待人工确认");
                return CommentStatus.PENDING;
            } else {
                log.info("AI 检测到违规内容，标记为待审核");
                return CommentStatus.PENDING;
            }
        } catch (Exception e) {
            log.error("AI 审核失败，使用本地敏感词检测: {}", e.getMessage());
            return localAudit(comment);
        }
    }
    
    private String auditCommentByAi(String content) {
        String prompt = AUDIT_PROMPT.replace("{content}", content);
        
        ChatClient chatClient = chatClientBuilder.build();
        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();
        
        return response.trim().toUpperCase();
    }
    
    private CommentStatus localAudit(Comment comment) {
        String content = comment.getContent();
        
        if (containsSensitiveWords(content)) {
            log.info("本地检测到敏感词，标记为待审核");
            return CommentStatus.PENDING;
        }
        
        if (auditConfig.getAiMode() == CommentAuditConfig.AiMode.AUTO) {
            log.info("本地检测模式，评论正常，直接通过");
            return CommentStatus.APPROVED;
        }
        
        log.info("本地检测模式，标记为待审核，等待人工确认");
        return CommentStatus.PENDING;
    }
    
    private boolean containsSensitiveWords(String content) {
        String[] sensitiveWords = {"广告", "推销", "垃圾评论"};
        
        for (String word : sensitiveWords) {
            if (content.contains(word)) {
                return true;
            }
        }
        
        return false;
    }
}
