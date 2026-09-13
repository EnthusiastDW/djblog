package site.dengwei.blog.presence;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * 处理在线状态相关的 STOMP 消息映射
 * <p>
 * 提供心跳接口和在线人数查询接口。
 * </p>
 *
 * @author dengwei
 * @since 2026-08-31
 */
@Controller
public class PresenceMessageController {

    private final PresenceRegistry registry;
    private final SimpMessagingTemplate messagingTemplate;

    public PresenceMessageController(PresenceRegistry registry, SimpMessagingTemplate messagingTemplate) {
        this.registry = registry;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 接收客户端心跳消息
     * <p>
     * 客户端发送 {clientId} 到 /app/presence/heartbeat，
     * 服务端验证 clientId 是否已注册，若已注册则更新对应 session 的心跳时间。
     * </p>
     *
     * @param payload 包含 clientId 的消息体
     * @param accessor 消息头访问器，用于获取 sessionId
     */
    @MessageMapping("/presence/heartbeat")
    public void handleHeartbeat(Map<String, String> payload, SimpMessageHeaderAccessor accessor) {
        String clientId = payload.get("clientId");
        String sessionId = accessor.getSessionId();

        if (clientId == null || sessionId == null) {
            return;
        }

        String registeredClientId = registry.getClientId(sessionId);
        if (clientId.equals(registeredClientId)) {
            registry.heartbeat(sessionId);
        }
    }

    /**
     * 客户端请求当前在线人数快照
     * <p>
     * 客户端发送空消息到 /app/presence/count，服务端返回当前在线人数。
     * </p>
     */
    @MessageMapping("/presence/count")
    public void requestOnlineCount() {
        messagingTemplate.convertAndSend(
                "/topic/online-count",
                Map.of("count", registry.getOnlineCount())
        );
    }
}