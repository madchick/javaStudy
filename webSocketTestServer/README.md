# 🚀 WebSocket Test Server Stack (Server + Client + Docker)

Nginx 웹/리버스 프록시, Linux(Ubuntu) 도커 컨테이너, **Spring Boot 기반 WebSocket 백엔드 서버**, 그리고 **전체 기능 테스트 웹 클라이언트**가 통합된 환경입니다.

---

## 📦 시스템 구성 및 포트 정보

| 서비스 | 위치 / 실행 방식 | 내부 포트 | 호스트 외부 포트 | 설명 |
|---|---|---|---|---|
| **Spring Boot Backend** | `server/` (로컬 앱) | - | `http://localhost:8080` | Spring Boot + Java 21 Toolchain 기반 WebSocket 서버 |
| **Test Client** | `client/index.html` | - | 로컬 브라우저 실행 | WebSocket 및 REST 전 기능 시각화 테스트 대시보드 |
| **Nginx Proxy** | `docker/` (`ws-nginx`) | 80 | `http://localhost:80` | 웹 서빙 및 WebSocket 리버스 프록시 (`/ws/` ➔ Spring Boot) |
| **Linux Container** | `docker/` (`ws-linux`) | - | - | 공용 이미지 `linux-ubuntu` (`websocat`, `curl`, `psql`, `redis-cli` 등) |

---

## 📁 디렉터리 및 파일 구조

```text
webSocketTestServer/
├── client/
│   └── index.html               # ⭐ WebSocket 전체 기능 테스트 대시보드 (브라우저 직접 실행)
├── server/                      # Spring Boot 백엔드 서버 프로젝트
│   ├── build.gradle             # Spring Boot 및 Java 21 Toolchain 빌드 설정
│   ├── settings.gradle          # Gradle 설정 (Foojay 1.0.0 리졸버 적용)
│   ├── gradlew / gradlew.bat    # Gradle 9.7 실행 래퍼
│   ├── WEBSOCKET_SPEC.md        # ⭐ 클라이언트 개발자용 상세 연동 규격서
│   └── src/
│       └── main/
│           ├── java/com/example/websocket/
│           │   ├── WebSocketServerApplication.java    # 메인 실행 클래스
│           │   ├── config/
│           │   │   ├── WebSocketConfig.java           # /ws, /ws/, / 엔드포인트 및 CORS 매핑
│           │   │   └── WebSocketHandshakeInterceptor.java # 클라이언트 IP 및 쿼리 파라미터 파싱
│           │   ├── dto/
│           │   │   ├── MessageType.java               # 메시지 타입 Enum (PING, ECHO, BROADCAST, ROOM 등)
│           │   │   ├── WsMessage.java                 # 표준 메시지 Envelope Record
│           │   │   └── ClientSessionInfo.java         # 세션 메타데이터
│           │   ├── handler/
│           │   │   └── CustomWebSocketHandler.java    # 세션 관리, 브로드캐스트, 룸 채널 라우팅
│           │   └── controller/
│           │       └── ServerStatusController.java    # 서버 상태 및 통계 REST API (/api/status)
│           └── resources/
│               └── application.yml                    # 서버 포트(8080) 및 로깅 설정
├── docker/                      # 컨테이너 인프라 환경
│   ├── docker-compose.yml       # Nginx & Linux 컨테이너 정의 파일
│   ├── linux/
│   │   └── Dockerfile           # Ubuntu 기반 네트워크/WebSocket 도구 이미지 정의
│   └── nginx/
│       ├── nginx.conf           # WebSocket 리버스 프록시 및 MIME 설정
│       └── html/
│           └── index.html       # Nginx 기본 서빙 테스트 웹 화면
└── README.md                    # 전체 프로젝트 통합 안내 매뉴얼 (본 문서)
```

---

## 🚀 서버 실행 및 빌드 방법

### 1. Spring Boot 백엔드 서버 가동
`server/` 디렉터리로 이동 후 Gradle Wrapper로 실행합니다:

- **macOS / Linux**:
  ```bash
  cd server
  ./gradlew bootRun
  ```
- **Windows (PowerShell / CMD)**:
  ```powershell
  cd server
  .\gradlew.bat bootRun
  ```
- **인텔리J IDEA**:
  - 우측 **Gradle** 탭 ➔ `Tasks` ➔ `application` ➔ `bootRun` 더블 클릭
  - 또는 [`WebSocketServerApplication.java`](./server/src/main/java/com/example/websocket/WebSocketServerApplication.java)의 `main` 메서드 좌측 초록색 재생(▶) 버튼 클릭

> *서버가 가동되면 `http://localhost:8080/api/status`에서 상태를 확인할 수 있습니다.*

### 2. 단독 실행 가능한 JAR 파일 빌드
```powershell
cd server
.\gradlew.bat bootJar
# 생성 경로: server/build/libs/webSocketTestServer-0.0.1-SNAPSHOT.jar
# 실행 명령어: java -jar server/build/libs/webSocketTestServer-0.0.1-SNAPSHOT.jar
```

### 3. Nginx & Linux 도커 컨테이너 가동 (선택 사항)
`docker/` 디렉터리로 이동하여 실행합니다:
```bash
cd docker
docker compose up -d
```

---

## 🧪 WebSocket 전체 기능 테스트 방법

### 1. 전용 테스트 대시보드 (`client/index.html`) - 가장 추천! ⭐
별도의 웹 서버 가동 없이 **[`client/index.html`](./client/index.html) 파일을 크롬 등 브라우저로 더블 클릭**하여 즉시 실행할 수 있습니다.

#### 🎯 제공되는 8가지 핵심 테스트 기능
1. **연결 및 식별 관리**:
   - `ws://localhost:8080/ws` (직결) 및 `ws://localhost/ws/` (Nginx 프록시) 원클릭 프리셋
   - `?userId=...&nickname=...` 쿼리 파라미터 전달 및 세션 ID 자동 감지
   - 30초 주기 자동 하트비트(Ping) 토글 옵션
2. **지연시간(RTT) & 에코 테스트**:
   - **`PING`**: 서버에 핑을 보내고 `PONG` 응답을 받아 **네트워크 왕복 지연시간(RTT ms)** 을 상단 뱃지에 실시간 표시
   - **`ECHO`**: 전송한 페이로드가 서버를 거쳐 그대로 회신되는지 확인
3. **전체 브로드캐스트 (`BROADCAST`)**:
   - 접속 중인 모든 사용자에게 실시간 단체 공지 메시지 발송
4. **룸 / 채널 그룹 통신 (`ROOM_*`)**:
   - 룸 참가(`ROOM_JOIN`), 룸 퇴장(`ROOM_LEAVE`), 룸 전용 메시지(`ROOM_MESSAGE`)
   - 현재 참가 중인 룸 목록 태그 표시 및 원클릭 퇴장
5. **1:1 귓속말 (클라이언트 ➔ 개인 송신)**:
   - 룸 참가 여부와 완전히 무관하게, 특정 사용자의 **`userId`** 또는 **`sessionId`**를 지정하여 1:1 비밀 메시지 발송 (발신자에게 전송 성공 ACK 회신)
6. **서버 ➔ 특정 개인 단독 푸시 (`POST /api/send-to-user`)**: ⭐
   - 서버(관리자)에서 특정 개인(`userId` 또는 `sessionId`)을 지정하여 단독 푸시 알림/메시지 강제 발송
7. **백엔드 REST API 모니터링 연동**:
   - **서버 상태 (`/api/status`)**: 서버 가동 시간(Uptime), 세션 수, 룸 현황 실시간 조회
   - **세션 목록 (`/api/sessions`)**: 현재 접속된 모든 세션의 userId 및 IP를 조회하고, **[1:1 선택]** 또는 **[서버푸시]** 버튼 클릭 시 대상 ID 자동 입력
8. **실시간 통신 로그 콘솔**:
   - 송신(`➔ [SENT]`), 수신(`⬅ [RECV]`), 시스템(`ℹ [SYS]`), 에러(`✖ [ERR]`) 색상 구분
   - JSON 자동 보기 좋게 줄바꿈(Pretty Print), 타입별 필터링, 실시간 카운터

> [!TIP]
> 브라우저 탭을 2~3개 열어 서로 다른 사용자(`userA`, `userB`)로 접속한 뒤, 브로드캐스트나 1:1 귓속말, 서버 푸시를 발송해 보시면 실시간 양방향 통신 흐름을 한눈에 파악하실 수 있습니다.

---

### 2. Linux 컨테이너 내부 터미널(CLI) 테스트
컨테이너 내부(`docker exec -it ws-linux bash`)에서 내장된 `websocat` 도구를 통해 CLI로 WebSocket 서버와 바로 통신할 수 있습니다:

```bash
# Nginx 프록시를 통한 WebSocket 연결
websocat ws://nginx/ws/

# 호스트(내 PC) 백엔드 직접 연결
websocat ws://host.docker.internal:8080/ws

# 연결 후 JSON 메시지 직접 입력하여 송신:
{"type": "BROADCAST", "sender": "CLI테스터", "payload": "CLI에서 보냄"}
{"type": "PING", "payload": "ping"}
{"type": "ROOM_JOIN", "roomId": "dev-room", "sender": "CLI테스터"}
```

---

## 📖 클라이언트 개발 연동 규격서

Web, Android(Kotlin), iOS(Swift), Flutter 등 실제 클라이언트 애플리케이션 개발에 필요한 모든 메시지 규격, JSON 스키마, 페이로드 예시, 플랫폼별 샘플 코드는 **[`server/WEBSOCKET_SPEC.md`](./server/WEBSOCKET_SPEC.md)** 파일에 상세히 정리되어 있습니다.
