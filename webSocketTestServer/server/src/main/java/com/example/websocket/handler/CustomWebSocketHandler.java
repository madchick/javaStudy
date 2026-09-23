package com.example.websocket.handler;

import com.example.websocket.dto.ClientSessionInfo;
import com.example.websocket.dto.MessageType;
import com.example.websocket.dto.WsMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.EOFException;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 표준 WebSocket 메시지 처리 핸들러
 */
@Component
public class CustomWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomWebSocketHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 활성 세션 보관소: sessionId -> WebSocketSession
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // 사용자 식별자 매핑: userId -> sessionId
    private final Map<String, String> userSessionMap = new ConcurrentHashMap<>();

    // 세션별 접속 시각 및 메타데이터
    private final Map<String, Long> sessionConnectedTime = new ConcurrentHashMap<>();

    // 룸(채널)별 참가 세션 목록: roomId -> Set<sessionId>
    private final Map<String, Set<String>> roomSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        sessionConnectedTime.put(sessionId, System.currentTimeMillis());

        String clientIp = (String) session.getAttributes().getOrDefault("clientIp", "unknown");
        String userId = (String) session.getAttributes().getOrDefault("userId", sessionId);
        userSessionMap.put(userId, sessionId);

        log.info("[WS Connected] SessionId: {}, UserId: {}, IP: {}, Total: {}",
                sessionId, userId, clientIp, sessions.size());

        // 클라이언트에게 접속 환영 및 세션 정보 전송
        Map<String, Object> welcomeData = Map.of(
                "sessionId", sessionId,
                "userId", userId,
                "clientIp", clientIp,
                "serverTime", System.currentTimeMillis(),
                "message", "WebSocket 서버에 성공적으로 연결되었습니다."
        );
        sendMessage(session, WsMessage.of(MessageType.SYSTEM_NOTICE, "SYSTEM", welcomeData));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        log.debug("[WS Received] SessionId: {}, Payload: {}", session.getId(), payload);

        try {
            WsMessage wsMessage = objectMapper.readValue(payload, WsMessage.class);
            if (wsMessage.type() == null) {
                sendMessage(session, WsMessage.error("메시지 type 필드가 누락되었습니다."));
                return;
            }

            processMessage(session, wsMessage);
        } catch (Exception e) {
            log.warn("[WS Parse Error] Invalid JSON from SessionId {}: {}", session.getId(), e.getMessage());
            sendMessage(session, WsMessage.error("잘못된 JSON 포맷입니다: " + e.getMessage()));
        }
    }

    private void processMessage(WebSocketSession session, WsMessage msg) {
        String sessionId = session.getId();
        String sender = (msg.sender() != null && !msg.sender().isBlank()) ? msg.sender() : sessionId;

        switch (msg.type()) {
            case PING -> {
                // 하트비트 PING에 대해 PONG 즉시 응답
                log.debug("[WS Ping] Received from SessionId: {}, replying PONG", sessionId);
                sendMessage(session, WsMessage.of(MessageType.PONG, "SYSTEM", "pong"));
            }
            case ECHO -> {
                // 에코 테스트: 보낸 클라이언트에게 그대로 반환
                sendMessage(session, WsMessage.of(MessageType.ECHO, "SYSTEM", msg.payload()));
            }
            case BROADCAST -> {
                // 전체 접속자 브로드캐스트
                WsMessage broadcastMsg = WsMessage.of(MessageType.BROADCAST, sender, msg.payload());
                broadcastToAll(broadcastMsg);
            }
            case ROOM_JOIN -> {
                if (msg.roomId() == null || msg.roomId().isBlank()) {
                    sendMessage(session, WsMessage.error("ROOM_JOIN 요청 시 roomId가 필요합니다."));
                    return;
                }
                roomSessions.computeIfAbsent(msg.roomId(), k -> ConcurrentHashMap.newKeySet()).add(sessionId);
                log.info("[WS Room Join] Session: {} joined room: {}", sessionId, msg.roomId());

                sendMessage(session, WsMessage.room(MessageType.ROOM_JOIN, "SYSTEM", msg.roomId(),
                        "Room [" + msg.roomId() + "]에 참가했습니다."));

                // 해당 룸 참여자들에게 알림
                broadcastToRoom(msg.roomId(), WsMessage.room(MessageType.SYSTEM_NOTICE, "SYSTEM", msg.roomId(),
                        sender + " 님이 입장했습니다."));
            }
            case ROOM_LEAVE -> {
                if (msg.roomId() == null || msg.roomId().isBlank()) {
                    sendMessage(session, WsMessage.error("ROOM_LEAVE 요청 시 roomId가 필요합니다."));
                    return;
                }
                Set<String> members = roomSessions.get(msg.roomId());
                if (members != null) {
                    members.remove(sessionId);
                    if (members.isEmpty()) {
                        roomSessions.remove(msg.roomId());
                    }
                }
                sendMessage(session, WsMessage.room(MessageType.ROOM_LEAVE, "SYSTEM", msg.roomId(),
                        "Room [" + msg.roomId() + "]에서 퇴장했습니다."));

                broadcastToRoom(msg.roomId(), WsMessage.room(MessageType.SYSTEM_NOTICE, "SYSTEM", msg.roomId(),
                        sender + " 님이 퇴장했습니다."));
            }
            case ROOM_MESSAGE -> {
                if (msg.roomId() == null || msg.roomId().isBlank()) {
                    sendMessage(session, WsMessage.error("ROOM_MESSAGE 요청 시 roomId가 필요합니다."));
                    return;
                }
                WsMessage roomMsg = WsMessage.room(MessageType.ROOM_MESSAGE, sender, msg.roomId(), msg.payload());
                broadcastToRoom(msg.roomId(), roomMsg);
            }
            case DIRECT_MESSAGE -> {
                if (msg.target() == null || msg.target().isBlank()) {
                    sendMessage(session, WsMessage.error("DIRECT_MESSAGE 요청 시 target(수신자 userId 또는 sessionId)이 필요합니다."));
                    return;
                }
                WebSocketSession targetSession = findSession(msg.target());
                if (targetSession != null && targetSession.isOpen()) {
                    WsMessage dm = WsMessage.direct(sender, msg.target(), msg.payload());
                    sendMessage(targetSession, dm);
                    // 발신자에게 전송 성공 확인 회신
                    sendMessage(session, WsMessage.of(MessageType.SYSTEM_NOTICE, "SYSTEM",
                            Map.of("status", "DELIVERED", "target", msg.target(), "message", "1:1 메시지가 성공적으로 전달되었습니다.")));
                } else {
                    sendMessage(session, WsMessage.error("대상 사용자(" + msg.target() + ")가 현재 접속해 있지 않습니다."));
                }
            }
            default -> {
                sendMessage(session, WsMessage.error("지원하지 않는 메시지 타입입니다: " + msg.type()));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        sessionConnectedTime.remove(sessionId);

        String userId = (String) session.getAttributes().getOrDefault("userId", sessionId);
        userSessionMap.remove(userId);

        // 모든 룸에서 세션 제거
        roomSessions.forEach((roomId, memberSet) -> {
            if (memberSet.remove(sessionId)) {
                broadcastToRoom(roomId, WsMessage.room(MessageType.SYSTEM_NOTICE, "SYSTEM", roomId,
                        "세션 " + sessionId + " 님의 연결이 종료되었습니다."));
            }
        });
        roomSessions.entrySet().removeIf(entry -> entry.getValue().isEmpty());

        log.info("[WS Closed] SessionId: {}, Code: {}, Reason: {}, Remaining: {}",
                sessionId, status.getCode(), status.getReason(), sessions.size());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        String sessionId = (session != null) ? session.getId() : "unknown";

        if (isClientDisconnectException(exception)) {
            // 브라우저 탭 닫기, 새로고침, 네트워크 단절 등 클라이언트의 비정상/갑작스러운 연결 종료
            log.info("[WS Disconnect] Client closed connection abruptly (EOF/Reset). SessionId: {}", sessionId);
        } else {
            String errorMsg = (exception.getMessage() != null && !exception.getMessage().isBlank())
                    ? exception.getMessage()
                    : exception.getClass().getSimpleName();
            log.warn("[WS Transport Error] SessionId: {}, Error: {}", sessionId, errorMsg);
        }

        if (session != null && session.isOpen()) {
            try {
                session.close(CloseStatus.SERVER_ERROR);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 클라이언트 연결 해제/네트워크 단절로 인한 일반적인 예외인지 판별
     */
    private boolean isClientDisconnectException(Throwable exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof EOFException || current instanceof ClosedChannelException) {
                return true;
            }
            String className = current.getClass().getName();
            if (className.contains("ClientAbortException")) {
                return true;
            }
            String message = current.getMessage();
            if (message != null) {
                String lower = message.toLowerCase();
                if (lower.contains("broken pipe")
                        || lower.contains("connection reset")
                        || lower.contains("connection timed out")
                        || lower.contains("closed")
                        || lower.contains("forcibly closed")) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }

    // 단일 세션 전송 (동시성 동기화)
    public void sendMessage(WebSocketSession session, WsMessage message) {
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(message);
            synchronized (session) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(json));
                }
            }
        } catch (IOException e) {
            if (isClientDisconnectException(e)) {
                log.debug("[WS Send] Session {} already disconnected: {}", session.getId(), e.getMessage());
            } else {
                log.error("[WS Send Error] Failed to send message to {}: {}", session.getId(), e.getMessage());
            }
        }
    }

    // 전체 브로드캐스트
    public void broadcastToAll(WsMessage message) {
        sessions.values().forEach(session -> sendMessage(session, message));
    }

    // 룸 단위 브로드캐스트
    public void broadcastToRoom(String roomId, WsMessage message) {
        Set<String> memberIds = roomSessions.get(roomId);
        if (memberIds == null || memberIds.isEmpty()) return;

        for (String memberId : memberIds) {
            WebSocketSession session = sessions.get(memberId);
            if (session != null) {
                sendMessage(session, message);
            }
        }
    }

    // 통계 및 모니터링용 메서드
    public int getActiveSessionCount() {
        return sessions.size();
    }

    public List<ClientSessionInfo> getActiveSessions() {
        return sessions.values().stream().map(session -> {
            String sessionId = session.getId();
            String clientIp = (String) session.getAttributes().getOrDefault("clientIp", "unknown");
            String userId = (String) session.getAttributes().getOrDefault("userId", sessionId);
            long connectedAt = sessionConnectedTime.getOrDefault(sessionId, 0L);

            Set<String> joinedRooms = roomSessions.entrySet().stream()
                    .filter(e -> e.getValue().contains(sessionId))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());

            return new ClientSessionInfo(sessionId, clientIp, userId, connectedAt, joinedRooms);
        }).toList();
    }

    public Map<String, Integer> getRoomMemberCounts() {
        Map<String, Integer> map = new HashMap<>();
        roomSessions.forEach((room, set) -> map.put(room, set.size()));
        return map;
    }

    /**
     * userId 또는 sessionId로 대상 WebSocketSession 검색
     */
    public WebSocketSession findSession(String target) {
        if (target == null || target.isBlank()) return null;

        // 1. sessionId 직접 일치 여부 확인
        WebSocketSession session = sessions.get(target);
        if (session != null && session.isOpen()) {
            return session;
        }

        // 2. userId 맵에서 sessionId 조회
        String sid = userSessionMap.get(target);
        if (sid != null) {
            WebSocketSession userSession = sessions.get(sid);
            if (userSession != null && userSession.isOpen()) {
                return userSession;
            }
        }

        // 3. 세션 속성(userId, nickname) 일치 여부 스캔
        for (WebSocketSession s : sessions.values()) {
            if (s.isOpen()) {
                Object uId = s.getAttributes().get("userId");
                Object nick = s.getAttributes().get("nickname");
                if (target.equalsIgnoreCase(String.valueOf(uId)) || target.equalsIgnoreCase(String.valueOf(nick))) {
                    return s;
                }
            }
        }
        return null;
    }

    /**
     * 서버에서 특정 대상(userId 또는 sessionId)에게 메시지 직접 전송
     */
    public boolean sendToTarget(String target, WsMessage message) {
        WebSocketSession session = findSession(target);
        if (session != null && session.isOpen()) {
            sendMessage(session, message);
            return true;
        }
        return false;
    }
}
