package com.example.websocket.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 표준 WebSocket 메시지 Envelope 모델 (Java Record)
 *
 * @param type      메시지 구분 (PING, ECHO, BROADCAST, ROOM_MESSAGE 등)
 * @param sender    보낸 사람 ID 또는 닉네임
 * @param target    1:1 수신 대상 세션 ID 또는 사용자 식별자
 * @param roomId    룸 / 채널 ID (룸 단위 메시징 시 사용)
 * @param payload   실제 데이터 (문자열 또는 임의의 JSON 객체)
 * @param timestamp 발생 시각 (Unix Epoch milliseconds)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record WsMessage(
    MessageType type,
    String sender,
    String target,
    String roomId,
    Object payload,
    Long timestamp
) {
    public static WsMessage of(MessageType type, String sender, Object payload) {
        return new WsMessage(type, sender, null, null, payload, System.currentTimeMillis());
    }

    public static WsMessage room(MessageType type, String sender, String roomId, Object payload) {
        return new WsMessage(type, sender, null, roomId, payload, System.currentTimeMillis());
    }

    public static WsMessage direct(String sender, String target, Object payload) {
        return new WsMessage(MessageType.DIRECT_MESSAGE, sender, target, null, payload, System.currentTimeMillis());
    }

    public static WsMessage system(String message) {
        return new WsMessage(MessageType.SYSTEM_NOTICE, "SYSTEM", null, null, message, System.currentTimeMillis());
    }

    public static WsMessage error(String errorMessage) {
        return new WsMessage(MessageType.ERROR, "SYSTEM", null, null, errorMessage, System.currentTimeMillis());
    }
}
