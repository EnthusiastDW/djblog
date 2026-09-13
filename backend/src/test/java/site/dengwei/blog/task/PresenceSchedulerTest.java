package site.dengwei.blog.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.TestPropertySource;
import site.dengwei.blog.presence.PresenceRegistry;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = {
        "presence.timeout-seconds=2",
        "presence.sweep-seconds=1"
})
class PresenceSchedulerTest {

    @Autowired
    private PresenceRegistry registry;

    @Autowired
    private PresenceScheduler scheduler;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private SimpMessagingTemplate mockMessagingTemplate;

    @BeforeEach
    void setUp() {
        mockMessagingTemplate = mock(SimpMessagingTemplate.class);
        // 使用反射替换 scheduler 中的 messagingTemplate
        try {
            var field = PresenceScheduler.class.getDeclaredField("messagingTemplate");
            field.setAccessible(true);
            field.set(scheduler, mockMessagingTemplate);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Timeout(30)
    @org.junit.jupiter.api.DisplayName("无心跳的会话在超时后被清理, 在线数变为 0")
    void sessionWithoutHeartbeat_EvictedAfterTimeout_CountBecomesZero() {
        // Given: 注册一个会话, 不发送心跳
        registry.register("client-1", "session-1");
        assertEquals(1, registry.getOnlineCount());

        // When: 等待超过 timeout (2秒) + sweep 间隔 (1秒) 让定时任务执行
        await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    assertEquals(0, registry.getOnlineCount(), "会话应已过期清理");
                });

        // Then: 验证广播被调用
        verify(mockMessagingTemplate, atLeastOnce())
                .convertAndSend(Optional.ofNullable(eq("/topic/online-count")), any());
    }

    @Test
    @Timeout(30)
    @org.junit.jupiter.api.DisplayName("有心跳的会话在超时时间内保持在线")
    void sessionWithHeartbeat_RemainsOnlineWithinTimeout() {
        // Given: 注册一个会话并发送心跳
        registry.register("client-2", "session-2");
        registry.heartbeat("session-2");
        assertEquals(1, registry.getOnlineCount());

        // When: 等待 sweep 间隔 (1秒) 让定时任务执行一次, 但未超过 timeout (2秒)
        await().atMost(5, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    // 定时任务至少执行了一次
                    verify(mockMessagingTemplate, atLeastOnce())
                            .convertAndSend(anyString(), anyInt());
                });

        // Then: 会话仍在线 (未被清理)
        assertEquals(1, registry.getOnlineCount(), "有心跳的会话不应被清理");
        assertEquals("client-2", registry.getClientId("session-2"));
    }

    @Test
    @Timeout(30)
    @org.junit.jupiter.api.DisplayName("多会话混合场景: 仅无心跳的会话被清理")
    void mixedSessions_OnlyHeartbeatLessEvicted() {
        // Given: 注册 3 个会话, 其中 2 个有心跳, 1 个无心跳
        registry.register("client-1", "session-1");
        registry.register("client-2", "session-2");
        registry.register("client-3", "session-3");
        registry.heartbeat("session-1");
        registry.heartbeat("session-2");
        assertEquals(3, registry.getOnlineCount());

        // When: 等待超过 timeout (2秒) + sweep 间隔
        await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    assertEquals(2, registry.getOnlineCount(), "仅无心跳的会话应被清理");
                });

        // Then: 验证正确的会话被保留
        assertTrue(registry.isOnline("session-1"), "session-1 应在线");
        assertTrue(registry.isOnline("session-2"), "session-2 应在线");
        assertFalse(registry.isOnline("session-3"), "session-3 应已过期");

        // 验证广播最终计数为 2
        verify(mockMessagingTemplate, atLeastOnce())
                .convertAndSend(Optional.ofNullable(eq("/topic/online-count")), any());
    }
}