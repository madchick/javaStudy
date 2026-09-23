package com.example.websocket.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;

/**
 * WebSocket 핸드셰이크 가로채기 (Nginx 프록시 헤더 및 쿼리 파라미터 파싱)
 */
@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WebSocketHandshakeInterceptor.class);

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        String clientIp = resolveClientIp(request);
        attributes.put("clientIp", clientIp);

        // 쿼리 파라미터(예: ?userId=xxx&nickname=yyy) 추출
        URI uri = request.getURI();
        String query = uri.getQuery();
        if (query != null && !query.isBlank()) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=", 2);
                if (pair.length == 2) {
                    attributes.put(pair[0], pair[1]);
                }
            }
        }

        log.info("[WS Handshake] Connected from IP: {}, Query: {}", clientIp, query);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        if (exception != null) {
            log.error("[WS Handshake Error] {}", exception.getMessage(), exception);
        }
    }

    private String resolveClientIp(ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            var req = servletRequest.getServletRequest();
            String xForwardedFor = req.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isBlank()) {
                return xForwardedFor.split(",")[0].trim();
            }
            String xRealIp = req.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isBlank()) {
                return xRealIp.trim();
            }
            return req.getRemoteAddr();
        }
        return request.getRemoteAddress() != null ? request.getRemoteAddress().getHostString() : "unknown";
    }
}
