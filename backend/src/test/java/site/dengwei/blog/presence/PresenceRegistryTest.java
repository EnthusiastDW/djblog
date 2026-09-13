package site.dengwei.blog.presence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PresenceRegistry 测试类
 *
 * @author dengwei
 * @since 2026-08-31
 */
class PresenceRegistryTest {

    private PresenceRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new PresenceRegistry();
    }

    // ==================== 基础注册/注销测试 ====================

    @Test
    @DisplayName("注册 1 个 clientId + 1 个 sessionId -> count=1, 返回 true")
    void register_SingleClientSingleSession_ReturnsTrueAndCountOne() {
        // When
        boolean isNewUser = registry.register("client-1", "session-1");

        // Then
        assertTrue(isNewUser);
        assertEquals(1, registry.getOnlineCount());
        assertEquals("client-1", registry.getClientId("session-1"));
    }

    @Test
    @DisplayName("同 clientId 注册第 2 个 sessionId (多标签页) -> count 不变, 返回 false")
    void register_SameClientSecondSession_ReturnsFalseCountUnchanged() {
        // Given
        registry.register("client-1", "session-1");

        // When
        boolean isNewUser = registry.register("client-1", "session-2");

        // Then
        assertFalse(isNewUser);
        assertEquals(1, registry.getOnlineCount());
        assertEquals("client-1", registry.getClientId("session-1"));
        assertEquals("client-1", registry.getClientId("session-2"));
    }

    @Test
    @DisplayName("注册 2 个不同 clientId -> count=2")
    void register_TwoDifferentClients_CountTwo() {
        // When
        registry.register("client-1", "session-1");
        registry.register("client-2", "session-2");

        // Then
        assertEquals(2, registry.getOnlineCount());
    }

    @Test
    @DisplayName("注销多标签页中的一个 session -> count 不变; 注销最后一个 -> count=0")
    void unregister_MultiTabThenLastSession_CountUpdatesCorrectly() {
        // Given
        registry.register("client-1", "session-1");
        registry.register("client-1", "session-2");
        assertEquals(1, registry.getOnlineCount());

        // When - 注销第一个标签页
        boolean userWentOffline = registry.unregister("session-1");

        // Then - 用户仍在线 (还有 session-2)
        assertFalse(userWentOffline);
        assertEquals(1, registry.getOnlineCount());
        assertEquals("client-1", registry.getClientId("session-2"));
        assertNull(registry.getClientId("session-1"));

        // When - 注销最后一个标签页
        userWentOffline = registry.unregister("session-2");

        // Then - 用户离线
        assertTrue(userWentOffline);
        assertEquals(0, registry.getOnlineCount());
        assertNull(registry.getClientId("session-2"));
    }

    @Test
    @DisplayName("注销不存在的 sessionId -> 返回 false, count 不变")
    void unregister_NonExistentSession_ReturnsFalse() {
        // Given
        registry.register("client-1", "session-1");

        // When
        boolean result = registry.unregister("non-existent");

        // Then
        assertFalse(result);
        assertEquals(1, registry.getOnlineCount());
    }

    // ==================== 心跳与过期清理测试 ====================

    @Test
    @DisplayName("心跳更新后 evictExpired -> session 保留")
    void heartbeat_ThenEvictExpired_SessionRetained() {
        // Given
        registry.register("client-1", "session-1");
        long now = System.currentTimeMillis();
        registry.heartbeat("session-1");

        // When - 立即清理 (timeout 1秒, 刚心跳过)
        List<String> evicted = registry.evictExpired(1000, now + 500);

        // Then
        assertTrue(evicted.isEmpty());
        assertEquals(1, registry.getOnlineCount());
        assertEquals("client-1", registry.getClientId("session-1"));
    }

    @Test
    @DisplayName("无心跳且超时 -> evictExpired 返回被清理的 sessionId 列表")
    void noHeartbeat_ThenEvictExpired_SessionEvicted() {
        // Given
        registry.register("client-1", "session-1");
        long now = System.currentTimeMillis();

        // When - 2秒后清理, timeout 1秒
        List<String> evicted = registry.evictExpired(1000, now + 2000);

        // Then
        assertEquals(1, evicted.size());
        assertEquals("session-1", evicted.get(0));
        assertEquals(0, registry.getOnlineCount());
        assertNull(registry.getClientId("session-1"));
    }

    @Test
    @DisplayName("多 session 过期清理 -> 只清理过期的, 保留活跃的")
    void evictExpired_MultipleSessions_OnlyExpiredRemoved() {
        registry.register("client-1", "session-1");
        registry.register("client-2", "session-2");
        registry.register("client-3", "session-3");
        long now = System.currentTimeMillis();

        // session-1 和 session-3 心跳
        registry.heartbeat("session-1");
        registry.heartbeat("session-3");

        // threshold = (now+500) - 1000 = now-500, 所有心跳 ≈ now > now-500 → 保留
        List<String> evicted = registry.evictExpired(1000, now + 500);
        assertEquals(0, evicted.size());
        assertEquals(3, registry.getOnlineCount());

        // threshold = (now+2000) - 500 = now+1500, 所有心跳 ≈ now < now+1500 → 全部过期
        evicted = registry.evictExpired(500, now + 2000);
        assertEquals(3, evicted.size());
        assertEquals(0, registry.getOnlineCount());
    }

    @Test
    @DisplayName("心跳不存在的 sessionId -> 静默忽略")
    void heartbeat_NonExistentSession_Ignored() {
        // When
        registry.heartbeat("non-existent");

        // Then - 无异常, 无副作用
        assertEquals(0, registry.getOnlineCount());
    }

    // ==================== 容量限制测试 ====================

    @Test
    @DisplayName("注册 10001 个不同 clientId -> 第 10001 个抛出 IllegalStateException")
    void register_CapacityLimit_10001stRejected() {
        for (int i = 0; i < 10000; i++) {
            registry.register("client-" + i, "session-" + i);
        }
        assertEquals(10000, registry.getOnlineCount());

        assertThrows(IllegalStateException.class,
                () -> registry.register("client-10000", "session-10000"));
        assertEquals(10000, registry.getOnlineCount());
    }

    @Test
    @DisplayName("同 clientId 多 session 不计入容量限制")
    void register_SameClientMultipleSessions_NotCountedInCapacity() {
        // Given - 注册 10000 个不同 clientId
        for (int i = 0; i < 10000; i++) {
            registry.register("client-" + i, "session-" + i);
        }

        // When - 给 client-0 再加 100 个 session
        for (int i = 0; i < 100; i++) {
            boolean result = registry.register("client-0", "extra-session-" + i);
            assertFalse(result, "同 clientId 多 session 应返回 false");
        }

        // Then
        assertEquals(10000, registry.getOnlineCount());
    }

    // ==================== 并发测试 ====================

    @Test
    @DisplayName("并发测试: 100 线程并发注册/注销不同 clientId, 最终 count 正确")
    void concurrentRegisterUnregister_DifferentClients_FinalCountCorrect() throws InterruptedException {
        // Given
        int threadCount = 100;
        int operationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // When
        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < operationsPerThread; i++) {
                        String clientId = "client-" + threadId + "-" + i;
                        String sessionId = "session-" + threadId + "-" + i;

                        // 注册
                        if (registry.register(clientId, sessionId)) {
                            successCount.incrementAndGet();
                        }

                        // 立即注销
                        registry.unregister(sessionId);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Then - 所有注册都应该成功 (每个 clientId 只注册一次)
        assertEquals(threadCount * operationsPerThread, successCount.get());
        assertEquals(0, registry.getOnlineCount(), "所有 session 都已注销, count 应为 0");
    }

    @Test
    @DisplayName("并发测试: 多线程对同一 clientId 注册多 session, count 为 1")
    void concurrentRegisterSameClient_MultiTab_CountOne() throws InterruptedException {
        // Given
        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AtomicInteger newUserCount = new AtomicInteger(0);

        // When - 所有线程尝试为同一 clientId 注册不同 session
        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    String sessionId = "session-" + threadId;
                    if (registry.register("shared-client", sessionId)) {
                        newUserCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Then - 只有第一个注册返回 true, count = 1
        assertEquals(1, newUserCount.get());
        assertEquals(1, registry.getOnlineCount());

        // 清理
        for (int t = 0; t < threadCount; t++) {
            registry.unregister("session-" + t);
        }
        assertEquals(0, registry.getOnlineCount());
    }

    @Test
    @DisplayName("并发测试: 并发心跳与清理不抛异常")
    void concurrentHeartbeatAndEvict_NoException() throws InterruptedException {
        // Given
        int sessionCount = 1000;
        for (int i = 0; i < sessionCount; i++) {
            registry.register("client-" + i, "session-" + i);
        }

        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount * 2);

        // When - 一半线程发心跳, 一半线程清理
        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            if (t % 2 == 0) {
                // 心跳线程
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        for (int i = 0; i < sessionCount; i++) {
                            registry.heartbeat("session-" + i);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        endLatch.countDown();
                    }
                });
            } else {
                // 清理线程
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        long now = System.currentTimeMillis();
                        registry.evictExpired(5000, now); // 5秒 timeout, 刚注册不久
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        endLatch.countDown();
                    }
                });
            }
        }

        startLatch.countDown();
        endLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Then - 无异常, 所有 session 保留 (未超时)
        assertEquals(sessionCount, registry.getOnlineCount());
    }

    // ==================== 边界情况测试 ====================

    @Test
    @DisplayName("getClientId 不存在的 sessionId -> 返回 null")
    void getClientId_NonExistent_ReturnsNull() {
        assertNull(registry.getClientId("non-existent"));
    }

    @Test
    @DisplayName("register null clientId 或 sessionId -> 抛出异常")
    void register_NullParams_ThrowsException() {
        assertThrows(NullPointerException.class, () -> registry.register(null, "session-1"));
        assertThrows(NullPointerException.class, () -> registry.register("client-1", null));
        assertThrows(NullPointerException.class, () -> registry.register(null, null));
    }

    @Test
    @DisplayName("unregister null sessionId -> 抛出异常")
    void unregister_NullSessionId_ThrowsException() {
        assertThrows(NullPointerException.class, () -> registry.unregister(null));
    }

    @Test
    @DisplayName("heartbeat null sessionId -> 抛出异常")
    void heartbeat_NullSessionId_ThrowsException() {
        assertThrows(NullPointerException.class, () -> registry.heartbeat(null));
    }

    @Test
    @DisplayName("evictExpired 负数 timeout -> 抛出异常")
    void evictExpired_NegativeTimeout_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> registry.evictExpired(-1, System.currentTimeMillis()));
    }

    @Test
    @DisplayName("evictExpired 负数 now -> 抛出异常")
    void evictExpired_NegativeNow_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> registry.evictExpired(1000, -1));
    }
}