package site.dengwei.blog.presence;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 线程安全的在线用户注册表
 * <p>
 * 使用 clientId (浏览器 UUID) 标识用户, sessionId (WebSocket 连接) 标识连接。
 * 支持多标签页去重: 同一 clientId 的多个 sessionId 视为同一用户在线。
 * 支持心跳机制与超时清理。
 * </p>
 *
 * @author dengwei
 * @since 2026-08-31
 */
@Component
public class PresenceRegistry {

    private static final int MAX_CLIENTS = 10000;

    /**
     * clientId -> sessionId 集合 (支持多标签页)
     * 使用 CopyOnWriteArraySet 保证并发安全的迭代和修改
     */
    private final Map<String, Set<String>> clientSessions = new ConcurrentHashMap<>();

    /**
     * sessionId -> 最后心跳时间戳 (毫秒)
     */
    private final Map<String, Long> sessionHeartbeats = new ConcurrentHashMap<>();

    /**
     * sessionId -> clientId 反向索引 (用于快速查找)
     */
    private final Map<String, String> sessionToClient = new ConcurrentHashMap<>();

    /**
     * 注册一个新的会话
     *
     * @param clientId 客户端 ID (浏览器 UUID)
     * @param sessionId 会话 ID (WebSocket 连接 ID)
     * @return true 如果是新上线用户 (clientId 计数 +1), false 如果是已在线用户的新标签页
     * @throws NullPointerException 如果 clientId 或 sessionId 为 null
     * @throws IllegalStateException 如果已达到最大客户端数量限制 (10000)
     */
    public boolean register(String clientId, String sessionId) {
        if (clientId == null || sessionId == null) {
            throw new NullPointerException("clientId and sessionId must not be null");
        }

        boolean[] isNewClient = {false};

        clientSessions.compute(clientId, (key, existing) -> {
            if (existing == null) {
                if (clientSessions.size() >= MAX_CLIENTS) {
                    throw new IllegalStateException("Maximum client capacity reached: " + MAX_CLIENTS);
                }
                isNewClient[0] = true;
                CopyOnWriteArraySet<String> set = new CopyOnWriteArraySet<>();
                set.add(sessionId);
                sessionToClient.put(sessionId, clientId);
                sessionHeartbeats.put(sessionId, System.currentTimeMillis());
                return set;
            } else {
                boolean added = existing.add(sessionId);
                if (added) {
                    sessionToClient.put(sessionId, clientId);
                    sessionHeartbeats.put(sessionId, System.currentTimeMillis());
                }
                return existing;
            }
        });

        return isNewClient[0];
    }

    /**
     * 注销一个会话
     *
     * @param sessionId 会话 ID
     * @return true 如果用户完全离线 (该 clientId 无其他 session), false 如果用户仍有其他标签页在线
     * @throws NullPointerException 如果 sessionId 为 null
     */
    public boolean unregister(String sessionId) {
        if (sessionId == null) {
            throw new NullPointerException("sessionId must not be null");
        }

        String clientId = sessionToClient.remove(sessionId);
        if (clientId == null) {
            return false; // session 不存在
        }

        sessionHeartbeats.remove(sessionId);

        Set<String> sessions = clientSessions.get(clientId);
        if (sessions != null) {
            sessions.remove(sessionId);
            // 如果该 clientId 下无其他 session, 移除 clientId
            if (sessions.isEmpty()) {
                clientSessions.remove(clientId);
                return true; // 用户完全离线
            }
        }

        return false; // 用户仍有其他标签页在线
    }

    /**
     * 更新会话的心跳时间戳
     *
     * @param sessionId 会话 ID
     * @throws NullPointerException 如果 sessionId 为 null
     */
    public void heartbeat(String sessionId) {
        if (sessionId == null) {
            throw new NullPointerException("sessionId must not be null");
        }

        // 只更新存在的 session 的心跳
        if (sessionToClient.containsKey(sessionId)) {
            sessionHeartbeats.put(sessionId, System.currentTimeMillis());
        }
    }

    /**
     * 获取当前在线用户数 (不同 clientId 的数量)
     *
     * @return 在线用户数
     */
    public int getOnlineCount() {
        return clientSessions.size();
    }

    /**
     * 清理过期会话
     *
     * @param timeoutMs 超时时间 (毫秒)
     * @param now 当前时间戳 (毫秒)
     * @return 被清理的 sessionId 列表 (用于广播下线通知)
     * @throws IllegalArgumentException 如果 timeoutMs < 0 或 now < 0
     */
    public List<String> evictExpired(long timeoutMs, long now) {
        if (timeoutMs < 0) {
            throw new IllegalArgumentException("timeoutMs must be non-negative");
        }
        if (now < 0) {
            throw new IllegalArgumentException("now must be non-negative");
        }

        long threshold = now - timeoutMs;
        List<String> evicted = new ArrayList<>();

        // 遍历所有 session, 找出过期的
        for (Map.Entry<String, Long> entry : sessionHeartbeats.entrySet()) {
            String sessionId = entry.getKey();
            long lastHeartbeat = entry.getValue();

            if (lastHeartbeat < threshold) {
                // 会话过期, 执行注销逻辑
                String clientId = sessionToClient.remove(sessionId);
                sessionHeartbeats.remove(sessionId);

                if (clientId != null) {
                    Set<String> sessions = clientSessions.get(clientId);
                    if (sessions != null) {
                        sessions.remove(sessionId);
                        if (sessions.isEmpty()) {
                            clientSessions.remove(clientId);
                        }
                    }
                    evicted.add(sessionId);
                }
            }
        }

        return evicted;
    }

    /**
     * 根据 sessionId 查找对应的 clientId
     *
     * @param sessionId 会话 ID
     * @return 对应的 clientId, 如果不存在则返回 null
     * @throws NullPointerException 如果 sessionId 为 null
     */
    public String getClientId(String sessionId) {
        if (sessionId == null) {
            throw new NullPointerException("sessionId must not be null");
        }
        return sessionToClient.get(sessionId);
    }

    /**
     * 获取指定 clientId 的所有 sessionId (用于广播等场景)
     *
     * @param clientId 客户端 ID
     * @return 不可修改的 sessionId 集合, 如果 clientId 不存在则返回空集合
     * @throws NullPointerException 如果 clientId 为 null
     */
    public Set<String> getSessions(String clientId) {
        if (clientId == null) {
            throw new NullPointerException("clientId must not be null");
        }
        Set<String> sessions = clientSessions.get(clientId);
        return sessions != null ? Collections.unmodifiableSet(sessions) : Collections.emptySet();
    }

    /**
     * 检查指定 sessionId 是否在线
     *
     * @param sessionId 会话 ID
     * @return true 如果在线
     * @throws NullPointerException 如果 sessionId 为 null
     */
    public boolean isOnline(String sessionId) {
        if (sessionId == null) {
            throw new NullPointerException("sessionId must not be null");
        }
        return sessionToClient.containsKey(sessionId);
    }

    /**
     * 检查指定 clientId 是否在线
     *
     * @param clientId 客户端 ID
     * @return true 如果在线 (至少有一个 session)
     * @throws NullPointerException 如果 clientId 为 null
     */
    public boolean isClientOnline(String clientId) {
        if (clientId == null) {
            throw new NullPointerException("clientId must not be null");
        }
        Set<String> sessions = clientSessions.get(clientId);
        return sessions != null && !sessions.isEmpty();
    }
}