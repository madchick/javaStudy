package com.example.websocket.dto;

/**
 * WebSocket 메시지 타입 열거형
 */
public enum MessageType {
    // 1. 하트비트 / 핑퐁
    PING,
    PONG,

    // 2. 단독 에코 테스트 (송신자에게 그대로 회신)
    ECHO,

    // 3. 전체 브로드캐스트 (모든 접속자에게 전송)
    BROADCAST,

    // 4. 룸 / 채널 관련
    ROOM_JOIN,
    ROOM_LEAVE,
    ROOM_MESSAGE,

    // 5. 1:1 다이렉트 메시지
    DIRECT_MESSAGE,

    // 6. 서버 시스템 알림 및 오류
    SYSTEM_NOTICE,
    ERROR
}
