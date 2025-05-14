package com.ztma.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // client subscribes here
        config.setApplicationDestinationPrefixes("/app"); // client sends messages here
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/chat-websocket").setAllowedOriginPatterns("*").withSockJS();
    }

    // 🔧 THIS IS THE NEW PART — increase buffer & message size limits
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration
            .setMessageSizeLimit(1024 * 1024)           // 1 MB max message size
            .setSendBufferSizeLimit(1024 * 1024)        // 1 MB send buffer
            .setSendTimeLimit(20 * 1000);               // 20 sec max send time
    }
}
