package com.example.websocket.controller;

import com.example.websocket.dto.ClientSessionInfo;
import com.example.websocket.dto.MessageType;
import com.example.websocket.dto.WsMessage;
import com.example.websocket.handler.CustomWebSocketHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Map;

/**
 * 서버 상태 모니터링 및 REST 기반 메시지 발행 컨트롤러
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ServerStatusController {

    private final CustomWebSocketHandler webSocketHandler;

    public ServerStatusController(CustomWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    /**
     * 서버 상태 및 통계 확인
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();

        Map<String, Object> status = Map.of(
                "status", "UP",
                "uptimeSeconds", uptimeMs / 1000,
                "activeSessions", webSocketHandler.getActiveSessionCount(),
                "rooms", webSocketHandler.getRoomMemberCounts(),
                "timestamp", System.currentTimeMillis()
        );
        return ResponseEntity.ok(status);
    }

    /**
     * 현재 접속 중인 세션 목록 조회
     */
    @GetMapping("/sessions")
    public ResponseEntity<List<ClientSessionInfo>> getSessions() {
        return ResponseEntity.ok(webSocketHandler.getActiveSessions());
    }

    /**
     * REST API를 통한 WebSocket 전체 브로드캐스트 공지 전송
     */
    @PostMapping("/broadcast")
    public ResponseEntity<Map<String, Object>> broadcast(@RequestBody Map<String, Object> body) {
        Object payload = body.getOrDefault("message", body);
        WsMessage msg = WsMessage.of(MessageType.BROADCAST, "REST_API_ADMIN", payload);
        webSocketHandler.broadcastToAll(msg);

        return ResponseEntity.ok(Map.of(
                "result", "SUCCESS",
                "broadcastTo", webSocketHandler.getActiveSessionCount() + " sessions"
        ));
    }

    /**
     * 서버에서 특정 개인(userId 또는 sessionId)에게 단독 메시지 푸시 전송
     */
    @PostMapping("/send-to-user")
    public ResponseEntity<Map<String, Object>> sendToUser(@RequestBody Map<String, Object> body) {
        String target = (String) body.get("target");
        Object payload = body.getOrDefault("message", body);
        String sender = (String) body.getOrDefault("sender", "SERVER_ADMIN");

        if (target == null || target.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "result", "FAIL",
                    "error", "target (수신자 userId 또는 sessionId)이 필요합니다."
            ));
        }

        WsMessage msg = WsMessage.direct(sender, target, payload);
        boolean sent = webSocketHandler.sendToTarget(target, msg);

        if (sent) {
            return ResponseEntity.ok(Map.of(
                    "result", "SUCCESS",
                    "target", target,
                    "sender", sender,
                    "message", payload,
                    "timestamp", System.currentTimeMillis()
            ));
        } else {
            return ResponseEntity.status(404).body(Map.of(
                    "result", "FAIL",
                    "error", "대상 사용자(" + target + ")가 현재 접속해 있지 않거나 찾을 수 없습니다."
            ));
        }
    }
}
