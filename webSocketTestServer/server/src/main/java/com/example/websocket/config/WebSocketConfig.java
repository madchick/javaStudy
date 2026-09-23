package com.example.websocket.config;

import com.example.websocket.handler.CustomWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 핸들러 및 엔드포인트 설정
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final CustomWebSocketHandler webSocketHandler;
    private final WebSocketHandshakeInterceptor handshakeInterceptor;

    public WebSocketConfig(CustomWebSocketHandler webSocketHandler,
                           WebSocketHandshakeInterceptor handshakeInterceptor) {
        this.webSocketHandler = webSocketHandler;
        this.handshakeInterceptor = handshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Nginx 프록시(/ws/) 및 직접 연결(/ws, /) 모두 처리할 수 있도록 복수 엔드포인트 등록
        registry.addHandler(webSocketHandler, "/ws", "/ws/", "/")
                .addInterceptors(handshakeInterceptor)
                .setAllowedOrigins("*");
    }
}
