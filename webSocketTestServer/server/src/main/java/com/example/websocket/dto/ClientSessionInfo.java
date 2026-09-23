package com.example.websocket.dto;

import java.util.Set;

/**
 * 접속 중인 클라이언트 세션 메타데이터
 */
public record ClientSessionInfo(
    String sessionId,
    String clientIp,
    String userId,
    long connectedAt,
    Set<String> joinedRooms
) {}
