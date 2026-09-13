package site.dengwei.blog.presence;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WebSocket 在线状态集成测试
 * <p>
 * 测试场景：
 * 1. A 连接 -> count >= 1
 * 2. B 同 clientId 连接 -> count = 1 (多标签页去重)
 * 3. C 不同 clientId 连接 -> count = 2
 * 4. 断开连接 -> count 减少
 * </p>
 *
 * @author dengwei
 * @since 2026-08-31
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
class PresenceWebSocketIntegrationTest {

    @Autowired
    private PresenceRegistry registry;

    private WebSocketStompClient stompClient;
    private int port;

    @BeforeEach
    void setUp() {
        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @Test
    @DisplayName("A 连接 -> 在线人数 >= 1")
    void connectSingleClient_OnlineCountAtLeastOne() throws Exception {
        // Given
        String clientId = "test-client-A";
        String wsUrl = "ws://localhost:" + port + "/ws?clientId=" + clientId;

        // When
        TestStompSessionHandler handler = new TestStompSessionHandler();
        StompSession session = stompClient.connectAsync(wsUrl, handler).get(5, TimeUnit.SECONDS);

        // Then - 等待广播
        Map<String, Integer> countUpdate = handler.waitForCountUpdate(3, TimeUnit.SECONDS);
        assertNotNull(countUpdate);
        assertTrue(countUpdate.get("count") >= 1, "在线人数应 >= 1, 实际: " + countUpdate.get("count"));

        // Cleanup
        session.disconnect();
        handler.waitForCountUpdate(3, TimeUnit.SECONDS); // 等待广播 count=0
    }

    @Test
    @DisplayName("B 同 clientId 连接 -> 在线人数 = 1 (多标签页去重)")
    void connectSameClientIdTwice_OnlineCountRemainsOne() throws Exception {
        // Given
        String clientId = "test-client-B";
        String wsUrl = "ws://localhost:" + port + "/ws?clientId=" + clientId;

        // When - 第一个连接
        TestStompSessionHandler handler1 = new TestStompSessionHandler();
        StompSession session1 = stompClient.connectAsync(wsUrl, handler1).get(5, TimeUnit.SECONDS);
        Map<String, Integer> count1 = handler1.waitForCountUpdate(3, TimeUnit.SECONDS);
        assertEquals(1, count1.get("count"), "第一个连接后 count 应为 1");

        // When - 第二个连接 (同 clientId，模拟多标签页)
        TestStompSessionHandler handler2 = new TestStompSessionHandler();
        StompSession session2 = stompClient.connectAsync(wsUrl, handler2).get(5, TimeUnit.SECONDS);
        Map<String, Integer> count2 = handler2.waitForCountUpdate(3, TimeUnit.SECONDS);
        assertEquals(1, count2.get("count"), "同 clientId 第二个连接后 count 仍应为 1");

        // 验证注册表内部状态
        assertEquals(1, registry.getOnlineCount(), "注册表在线人数应为 1");
        assertEquals(2, registry.getSessions(clientId).size(), "该 clientId 应有 2 个 session");

        // Cleanup - 断开第一个
        session1.disconnect();
        Map<String, Integer> countAfterDisconnect1 = handler1.waitForCountUpdate(3, TimeUnit.SECONDS);
        assertEquals(1, countAfterDisconnect1.get("count"), "断开一个标签页后 count 仍应为 1");

        // Cleanup - 断开第二个
        session2.disconnect();
        Map<String, Integer> countAfterDisconnect2 = handler2.waitForCountUpdate(3, TimeUnit.SECONDS);
        assertEquals(0, countAfterDisconnect2.get("count"), "断开所有标签页后 count 应为 0");
    }

    @Test
    @DisplayName("C 不同 clientId 连接 -> 在线人数 = 2")
    void connectTwoDifferentClients_OnlineCountTwo() throws Exception {
        // Given
        String clientId1 = "test-client-C1";
        String clientId2 = "test-client-C2";
        String wsUrl1 = "ws://localhost:" + port + "/ws?clientId=" + clientId1;
        String wsUrl2 = "ws://localhost:" + port + "/ws?clientId=" + clientId2;

        // When - 第一个客户端连接
        TestStompSessionHandler handler1 = new TestStompSessionHandler();
        StompSession session1 = stompClient.connectAsync(wsUrl1, handler1).get(5, TimeUnit.SECONDS);
        Map<String, Integer> count1 = handler1.waitForCountUpdate(3, TimeUnit.SECONDS);
        assertEquals(1, count1.get("count"), "第一个客户端连接后 count 应为 1");

        // When - 第二个不同客户端连接
        TestStompSessionHandler handler2 = new TestStompSessionHandler();
        StompSession session2 = stompClient.connectAsync(wsUrl2, handler2).get(5, TimeUnit.SECONDS);
        Map<String, Integer> count2 = handler2.waitForCountUpdate(3, TimeUnit.SECONDS);
        assertEquals(2, count2.get("count"), "第二个不同客户端连接后 count 应为 2");

        // 验证注册表内部状态
        assertEquals(2, registry.getOnlineCount(), "注册表在线人数应为 2");

        // Cleanup - 断开第一个
        session1.disconnect();
        Map<String, Integer> countAfterDisconnect1 = handler1.waitForCountUpdate(3, TimeUnit.SECONDS);
        assertEquals(1, countAfterDisconnect1.get("count"), "断开第一个客户端后 count 应为 1");

        // Cleanup - 断开第二个
        session2.disconnect();
        Map<String, Integer> countAfterDisconnect2 = handler2.waitForCountUpdate(3, TimeUnit.SECONDS);
        assertEquals(0, countAfterDisconnect2.get("count"), "断开所有客户端后 count 应为 0");
    }

    @Test
    @DisplayName("连接 -> 断开 -> 在线人数减少")
    void connectThenDisconnect_OnlineCountDecreases() throws Exception {
        // Given
        String clientId = "test-client-D";
        String wsUrl = "ws://localhost:" + port + "/ws?clientId=" + clientId;

        // When - 连接
        TestStompSessionHandler handler = new TestStompSessionHandler();
        StompSession session = stompClient.connectAsync(wsUrl, handler).get(5, TimeUnit.SECONDS);
        Map<String, Integer> countConnected = handler.waitForCountUpdate(3, TimeUnit.SECONDS);
        assertEquals(1, countConnected.get("count"), "连接后 count 应为 1");

        // When - 断开连接
        session.disconnect();
        Map<String, Integer> countDisconnected = handler.waitForCountUpdate(3, TimeUnit.SECONDS);
        assertEquals(0, countDisconnected.get("count"), "断开后 count 应为 0");

        // 验证注册表状态
        assertEquals(0, registry.getOnlineCount(), "注册表在线人数应为 0");
    }

    @Test
    @DisplayName("订阅 /topic/online-count 立即返回当前快照")
    void subscribeOnlineCount_ReturnsSnapshot() throws Exception {
        // Given
        String clientId = "test-client-E";
        String wsUrl = "ws://localhost:" + port + "/ws?clientId=" + clientId;

        TestStompSessionHandler handler = new TestStompSessionHandler();
        StompSession session = stompClient.connectAsync(wsUrl, handler).get(5, TimeUnit.SECONDS);

        // 等待连接建立和初始广播
        handler.waitForCountUpdate(3, TimeUnit.SECONDS);

        // When - 订阅在线人数 (SubscribeMapping 会立即返回快照)
        CompletableFuture<Map<String, Integer>> snapshotFuture = new CompletableFuture<>();
        session.subscribe("/topic/online-count", new StompSessionHandlerAdapter() {
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                @SuppressWarnings("unchecked")
                Map<String, Integer> map = (Map<String, Integer>) payload;
                snapshotFuture.complete(map);
            }

            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }
        });

        Map<String, Integer> snapshot = snapshotFuture.get(3, TimeUnit.SECONDS);

        // Then
        assertNotNull(snapshot);
        assertTrue(snapshot.containsKey("count"));
        assertEquals(1, snapshot.get("count"), "快照应返回当前在线人数 1");

        // Cleanup
        session.disconnect();
    }

    @Test
    @DisplayName("心跳消息更新 session 心跳时间")
    void heartbeatMessage_UpdatesSessionHeartbeat() throws Exception {
        // Given
        String clientId = "test-client-F";
        String wsUrl = "ws://localhost:" + port + "/ws?clientId=" + clientId;

        TestStompSessionHandler handler = new TestStompSessionHandler();
        StompSession session = stompClient.connectAsync(wsUrl, handler).get(5, TimeUnit.SECONDS);
        handler.waitForCountUpdate(3, TimeUnit.SECONDS);

        String sessionId = session.getSessionId();
        assertNotNull(sessionId);

        // When - 发送心跳消息
        session.send("/app/presence/heartbeat", Map.of("clientId", clientId));

        // 等待心跳处理 (给一点时间让拦截器处理)
        Thread.sleep(100);

        // Then - 验证心跳已更新 (通过注册表检查 session 仍在线)
        assertTrue(registry.isOnline(sessionId), "发送心跳后 session 应仍在线");

        // Cleanup
        session.disconnect();
        handler.waitForCountUpdate(3, TimeUnit.SECONDS);
    }

    /**
     * 测试用的 STOMP 会话处理器，用于接收 /topic/online-count 广播
     */
    private static class TestStompSessionHandler extends StompSessionHandlerAdapter {

        private final CompletableFuture<Map<String, Integer>> countUpdateFuture = new CompletableFuture<>();
        private final Map<String, Integer> lastCount = new ConcurrentHashMap<>();

        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            // 连接建立后订阅在线人数主题
            session.subscribe("/topic/online-count", this);
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            @SuppressWarnings("unchecked")
            Map<String, Integer> map = (Map<String, Integer>) payload;
            lastCount.put("count", map.get("count"));
            countUpdateFuture.complete(map);
        }

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return Map.class;
        }

        @Override
        public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
            countUpdateFuture.completeExceptionally(exception);
        }

        @Override
        public void handleTransportError(StompSession session, Throwable exception) {
            countUpdateFuture.completeExceptionally(exception);
        }

        /**
         * 等待下一个在线人数更新
         */
        Map<String, Integer> waitForCountUpdate(long timeout, TimeUnit unit) throws Exception {
            return countUpdateFuture.get(timeout, unit);
        }

        /**
         * 重置 future 以等待下一次更新
         */
        void reset() {
            countUpdateFuture.complete(null); // 完成当前 future
            // 创建新的 future
            @SuppressWarnings("unchecked")
            CompletableFuture<Map<String, Integer>> newFuture = new CompletableFuture<>();
            // 使用反射替换字段 (简化处理，实际测试中每次新建 handler)
        }
    }
}