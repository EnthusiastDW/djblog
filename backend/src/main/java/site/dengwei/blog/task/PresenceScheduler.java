package site.dengwei.blog.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import site.dengwei.blog.presence.PresenceRegistry;

import java.util.List;

/**
 * WebSocket 在线用户定时清理任务
 * <p>
 * 定期扫描并清理过期的会话, 当在线用户数发生变化时广播更新。
 * </p>
 *
 * @author dengwei
 * @since 2026-08-31
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class PresenceScheduler {

    private final PresenceRegistry registry;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${presence.timeout-seconds:90}")
    private long timeoutSeconds;

    @Value("${presence.sweep-seconds:60}")
    private long sweepSeconds;

    /**
     * 定期清理过期会话
     * <p>
     * 固定延迟执行, 间隔由 {@code presence.sweep-seconds} 配置 (默认 60 秒)。
     * 清理超时时间由 {@code presence.timeout-seconds} 配置 (默认 90 秒)。
     * 如果清理导致在线用户数变化, 通过 WebSocket 广播最新计数。
     * </p>
     */
    @Scheduled(fixedDelayString = "${presence.sweep-seconds:60}000")
    public void evictExpiredSessions() {
        long now = System.currentTimeMillis();
        long timeoutMs = timeoutSeconds * 1000L;

        int countBefore = registry.getOnlineCount();
        List<String> evicted = registry.evictExpired(timeoutMs, now);
        int countAfter = registry.getOnlineCount();

        if (!evicted.isEmpty()) {
            log.debug("清理过期会话: evicted={}, countBefore={}, countAfter={}", evicted.size(), countBefore, countAfter);
        }

        // 如果在线用户数发生变化, 广播最新计数
        if (countBefore != countAfter) {
            broadcastOnlineCount(countAfter);
        }
    }

    /**
     * 广播在线用户数到 /topic/presence/count
     *
     * @param count 当前在线用户数
     */
    private void broadcastOnlineCount(int count) {
        try {
            messagingTemplate.convertAndSend("/topic/online-count", java.util.Map.of("count", count));
            log.debug("广播在线用户数: {}", count);
        } catch (Exception e) {
            log.warn("广播在线用户数失败: {}", e.getMessage());
        }
    }
}