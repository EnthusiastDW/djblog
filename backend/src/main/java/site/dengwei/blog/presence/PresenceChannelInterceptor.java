package site.dengwei.blog.presence;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WebSocket 连接通道拦截器，用于在线状态跟踪
 * <p>
 * 拦截 STOMP 帧，处理连接建立、心跳、断开连接等事件，
 * 并通过 PresenceRegistry 维护在线用户状态。
 * </p>
 *
 * @author dengwei
 * @since 2026-08-31
 */
public class PresenceChannelInterceptor implements ChannelInterceptor {

    private static final int MAX_CLIENT_ID_LENGTH = 64;
    private static final String CLIENT_ID_HEADER = "clientId";

    private final PresenceRegistry registry;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    private final AtomicInteger lastBroadcastCount = new AtomicInteger(-1);

    /**
     * 构造函数
     *
     * @param registry          在线用户注册表
     * @param messagingTemplate 消息模板，用于广播在线人数变化
     */
    public PresenceChannelInterceptor(PresenceRegistry registry,
                                      org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate) {
        this.registry = registry;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        String sessionId = accessor.getSessionId();

        if (sessionId == null) {
            return message;
        }

        switch (command) {
            case CONNECT -> handleConnect(accessor, sessionId);
            case DISCONNECT -> handleDisconnect(sessionId);
            default -> {
                // 任何 STOMP 帧都更新心跳
                if (command != null) {
                    registry.heartbeat(sessionId);
                }
            }
        }

        return message;
    }

    /**
     * 处理 CONNECT 帧
     * 从 STOMP connectHeaders 读取 clientId，验证后注册到注册表
     */
    private void handleConnect(StompHeaderAccessor accessor, String sessionId) {
        // 从 STOMP connectHeaders 获取 clientId
        String clientId = getFirstNativeHeader(accessor, CLIENT_ID_HEADER);

        if (!StringUtils.hasText(clientId)) {
            // clientId 为空，拒绝连接
            throw new IllegalArgumentException("clientId is required and must not be empty");
        }

        if (clientId.length() > MAX_CLIENT_ID_LENGTH) {
            throw new IllegalArgumentException("clientId length must not exceed " + MAX_CLIENT_ID_LENGTH + " characters");
        }

        // 注册到注册表
        boolean isNewUser = registry.register(clientId, sessionId);

        // 如果在线人数发生变化，广播
        if (isNewUser) {
            broadcastOnlineCount();
        }
    }

    /**
     * 处理 DISCONNECT 帧
     * 从注册表注销 sessionId
     */
    private void handleDisconnect(String sessionId) {
        boolean userWentOffline = registry.unregister(sessionId);

        // 如果用户完全离线，广播在线人数变化
        if (userWentOffline) {
            broadcastOnlineCount();
        }
    }

    private void broadcastOnlineCount() {
        int currentCount = registry.getOnlineCount();
        if (lastBroadcastCount.compareAndSet(currentCount, currentCount)) {
            messagingTemplate.convertAndSend("/topic/online-count", Map.of("count", currentCount));
        }
    }

    /**
     * 从 nativeHeaders 获取第一个值
     */
    private String getFirstNativeHeader(StompHeaderAccessor accessor, String headerName) {
        if (accessor.getNativeHeader(headerName) != null && !accessor.getNativeHeader(headerName).isEmpty()) {
            return accessor.getNativeHeader(headerName).get(0);
        }
        return null;
    }
}