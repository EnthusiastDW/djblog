package site.dengwei.blog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import site.dengwei.blog.presence.PresenceChannelInterceptor;
import site.dengwei.blog.presence.PresenceRegistry;

/**
 * WebSocket STOMP 配置类
 *
 * @author dengwei
 * @since 2025/9/6
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final PresenceRegistry presenceRegistry;
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketConfig(PresenceRegistry presenceRegistry, @Lazy SimpMessagingTemplate messagingTemplate) {
        this.presenceRegistry = presenceRegistry;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setTimeToFirstMessage(30000);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new PresenceChannelInterceptor(presenceRegistry, messagingTemplate));
    }
}