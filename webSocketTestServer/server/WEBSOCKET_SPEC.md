# 📡 WebSocket 클라이언트 연동 규격서 (Client Integration Specification)

본 문서는 Nginx 리버스 프록시 및 Spring Boot 백엔드 기반 WebSocket 서버와 통신하는 **클라이언트(Web, Android, iOS, Flutter 등)** 개발을 위한 상세 연동 규격서입니다.

---

## 1. 개요 및 접속 엔드포인트

### 1.1 시스템 구조
```
[Client (Web/App)] ──> [Nginx Reverse Proxy :80] ──> [Spring Boot WebSocket Server :8080]
```

### 1.2 접속 URL
| 환경 | WebSocket URL | 설명 |
|---|---|---|
| **Nginx 프록시 경유 (기본)** | `ws://localhost/ws/` | Nginx 80번 포트를 통해 진입하며, WebSocket Upgrade 헤더를 거쳐 백엔드로 전달 |
| **백엔드 직접 연결 (개발/테스트)** | `ws://localhost:8080/ws` | Spring Boot 서버로 직접 직결 접속 |
| **운영 환경 (SSL/TLS 적용 시)** | `wss://<도메인>/ws/` | HTTPS 및 WSS 보안 프로토콜 적용 시 |

### 1.3 핸드셰이크 쿼리 파라미터 (선택 사항)
WebSocket 연결 초기 핸드셰이크 시점에 식별용 파라미터를 URL Query String으로 전달할 수 있습니다:
```
ws://localhost/ws/?userId=user_1024&nickname=홍길동
```
* 서버는 이 파라미터를 파싱하여 해당 세션의 속성(Attribute)에 보관하며, 접속 로그 및 메시지 발신자 기본값으로 활용합니다.

---

## 2. 메시지 프로토콜 규격 (JSON Envelope)

모든 데이터는 **UTF-8 문자열의 JSON 객체(Envelope)** 형태로 송수신됩니다.

### 2.1 표준 메시지 스키마
```json
{
  "type": "BROADCAST",
  "sender": "홍길동",
  "target": null,
  "roomId": null,
  "payload": "안녕하세요!",
  "timestamp": 1727077200000
}
```

### 2.2 필드 정의
| 필드명 | 타입 | 필수 여부 | 설명 |
|---|---|:---:|---|
| `type` | String | **필수** | 메시지 종류 (`PING`, `ECHO`, `BROADCAST`, `ROOM_JOIN`, `ROOM_MESSAGE`, `ROOM_LEAVE`, `DIRECT_MESSAGE` 등) |
| `sender` | String | 선택 | 발신자 식별자(아이디 또는 닉네임). 미지정 시 서버에서 할당한 세션 ID가 적용됩니다. |
| `target` | String | 조건부 | `DIRECT_MESSAGE` 시 수신 대상 세션 ID |
| `roomId` | String | 조건부 | 룸 관련 메시지(`ROOM_JOIN`, `ROOM_MESSAGE`, `ROOM_LEAVE`) 시 대상 방 식별자 |
| `payload` | Any (String, Object, Array 등) | 선택 | 실제 전달할 데이터 본문 |
| `timestamp` | Number (Long) | 선택/자동 | 발생 시각 (Unix Epoch milliseconds, 13자리) |

---

## 3. 메시지 타입별 상세 스펙 및 페이로드 예시

### 3.1 연결 성공 알림 (`SYSTEM_NOTICE`)
* **방향**: Server ➔ Client (접속 완료 직후 자동 전송)
* **설명**: 연결 수립 즉시 서버가 클라이언트에게 부여된 세션 ID와 접속 정보를 반환합니다.
```json
{
  "type": "SYSTEM_NOTICE",
  "sender": "SYSTEM",
  "payload": {
    "sessionId": "a8f3b129-e2b4",
    "userId": "user_1024",
    "clientIp": "127.0.0.1",
    "serverTime": 1727077200123,
    "message": "WebSocket 서버에 성공적으로 연결되었습니다."
  },
  "timestamp": 1727077200123
}
```

---

### 3.2 하트비트 (`PING` ➔ `PONG`)
* **목적**: 연결 생존 확인(Keep-Alive) 및 유휴 연결 끊김 방지 (권장 주기: 30초~60초)
* **클라이언트 요청 (`PING`)**:
  ```json
  {
    "type": "PING",
    "payload": "ping"
  }
  ```
* **서버 응답 (`PONG`)**:
  ```json
  {
    "type": "PONG",
    "sender": "SYSTEM",
    "payload": "pong",
    "timestamp": 1727077205100
  }
  ```

---

### 3.3 단독 에코 테스트 (`ECHO`)
* **목적**: 클라이언트-서버 간 RTT(Round Trip Time) 왕복 지연시간 측정 및 통신 단위 테스트
* **클라이언트 요청 (`ECHO`)**:
  ```json
  {
    "type": "ECHO",
    "payload": {
      "testId": 1,
      "content": "RTT 테스트 메시지"
    }
  }
  ```
* **서버 응답 (`ECHO`)**:
  * 보낸 클라이언트 본인에게만 동일한 `payload`가 그대로 회신됩니다.

---

### 3.4 전체 브로드캐스트 (`BROADCAST`)
* **목적**: 현재 서버에 접속 중인 **모든 클라이언트**에게 실시간 메시지 전송
* **클라이언트 요청 (`BROADCAST`)**:
  ```json
  {
    "type": "BROADCAST",
    "sender": "홍길동",
    "payload": "전체 공지: 회의가 10분 뒤 시작됩니다."
  }
  ```
* **수신 클라이언트들이 받는 메시지**:
  ```json
  {
    "type": "BROADCAST",
    "sender": "홍길동",
    "payload": "전체 공지: 회의가 10분 뒤 시작됩니다.",
    "timestamp": 1727077210550
  }
  ```

---

### 3.5 룸 / 채널 기반 통신 (`ROOM_*`)
특정 방(Channel/Room)에 가입한 사용자들끼리만 격리된 그룹 메시지를 주고받습니다.

#### ① 룸 입장 (`ROOM_JOIN`)
* **요청**:
  ```json
  {
    "type": "ROOM_JOIN",
    "sender": "홍길동",
    "roomId": "dev-chat"
  }
  ```
* **서버 응답**:
  * 요청자 본인: `ROOM_JOIN` 완료 메시지 수신
  * 룸 내 다른 참여자들: `"홍길동 님이 입장했습니다."` `SYSTEM_NOTICE` 수신

#### ② 룸 메시지 전송 (`ROOM_MESSAGE`)
* **요청**:
  ```json
  {
    "type": "ROOM_MESSAGE",
    "sender": "홍길동",
    "roomId": "dev-chat",
    "payload": "안녕하세요 개발팀 여러분!"
  }
  ```
* **수신 (룸 참여자 전원)**:
  ```json
  {
    "type": "ROOM_MESSAGE",
    "sender": "홍길동",
    "roomId": "dev-chat",
    "payload": "안녕하세요 개발팀 여러분!",
    "timestamp": 1727077220000
  }
  ```

#### ③ 룸 퇴장 (`ROOM_LEAVE`)
* **요청**:
  ```json
  {
    "type": "ROOM_LEAVE",
    "sender": "홍길동",
    "roomId": "dev-chat"
  }
  ```

---

### 3.6 1:1 다이렉트 메시지 (`DIRECT_MESSAGE`)
* **목적**: 룸(채널) 가입 여부와 무관하게, 특정 개인에게 1:1로 직접 메시지 전송
* **수신 대상 식별**: `target` 필드에 수신자의 **`userId`** 또는 **`sessionId`** 모두 지정 가능
* **클라이언트 요청**:
  ```json
  {
    "type": "DIRECT_MESSAGE",
    "sender": "홍길동",
    "target": "user_guest",
    "payload": "비밀 메시지입니다."
  }
  ```
* **수신자에게 전달되는 메시지**:
  ```json
  {
    "type": "DIRECT_MESSAGE",
    "sender": "홍길동",
    "target": "user_guest",
    "payload": "비밀 메시지입니다.",
    "timestamp": 1727077225000
  }
  ```
* **발신자에게 전달되는 전송 확인(ACK) 메시지**:
  * 메시지가 성공적으로 대상에게 전달되면, 서버가 발신자에게 아래와 같이 배달 완료 확인 알림을 회신합니다.
  ```json
  {
    "type": "SYSTEM_NOTICE",
    "sender": "SYSTEM",
    "payload": {
      "status": "DELIVERED",
      "target": "user_guest",
      "message": "1:1 메시지가 성공적으로 전달되었습니다."
    },
    "timestamp": 1727077225100
  }
  ```

---

### 3.7 에러 응답 (`ERROR`)
* 잘못된 JSON 형식, 지원하지 않는 타입, 필수 파라미터 누락, 존재하지 않는 수신자 대상 전송 시 서버에서 발송
```json
{
  "type": "ERROR",
  "sender": "SYSTEM",
  "payload": "대상 사용자(user_unknown)가 현재 접속해 있지 않습니다.",
  "timestamp": 1727077230000
}
```

---

## 4. 모니터링 및 REST API 보조 엔드포인트

HTTP REST API를 통해 WebSocket 상태를 조회하거나, **외부 백엔드/관리자 시스템에서 특정 개인 또는 전체 접속자에게 메시지를 직접 푸시**할 수 있습니다.

| Method | Endpoint | 설명 |
|---|---|---|
| `GET` | `/api/status` | 서버 상태, Uptime, 활성 세션 수, 룸별 참가자 수 조회 |
| `GET` | `/api/sessions` | 현재 접속된 모든 세션의 ID, IP, 접속 시각 목록 조회 |
| `POST` | `/api/broadcast` | REST API를 통해 전체 WebSocket 접속자에게 브로드캐스트 공지 푸시 |
| `POST` | `/api/send-to-user` | ⭐ REST API를 통해 **특정 개인(userId 또는 sessionId)** 에게 단독 푸시 메시지 발송 |

### 4.1 서버에서 특정 개인에게 단독 푸시 발송 (`POST /api/send-to-user`)
* **Request Body**:
  ```json
  {
    "target": "user_guest",
    "sender": "SERVER_ADMIN",
    "message": "고객님, 주문하신 결제가 정상 처리되었습니다."
  }
  ```
* **cURL 테스트 예시**:
  ```bash
  curl -X POST http://localhost:8080/api/send-to-user \
    -H "Content-Type: application/json" \
    -d '{
      "target": "user_guest",
      "message": "관리자 알림: 귀하의 계정에 새로운 알림이 도착했습니다."
    }'
  ```
* **Response (성공 시 200 OK)**:
  ```json
  {
    "result": "SUCCESS",
    "target": "user_guest",
    "sender": "SERVER_ADMIN",
    "message": "관리자 알림: 귀하의 계정에 새로운 알림이 도착했습니다.",
    "timestamp": 1727077240000
  }
  ```
* **Response (대상 미접속 시 404 Not Found)**:
  ```json
  {
    "result": "FAIL",
    "error": "대상 사용자(user_guest)가 현재 접속해 있지 않거나 찾을 수 없습니다."
  }
  ```

**`/api/broadcast` 요청 예시:**
```bash
curl -X POST http://localhost:8080/api/broadcast \
  -H "Content-Type: application/json" \
  -d '{"message": "서버 점검 예정 안내"}'
```

---

## 5. 플랫폼별 클라이언트 구현 예제

### 5.1 Web (JavaScript / TypeScript)
```javascript
class WsClient {
  constructor(url) {
    this.url = url;
    this.ws = null;
    this.pingInterval = null;
  }

  connect() {
    this.ws = new WebSocket(this.url);

    this.ws.onopen = () => {
      console.log('Connected to server');
      // 30초마다 PING 전송
      this.pingInterval = setInterval(() => {
        this.send({ type: 'PING', payload: 'ping' });
      }, 30000);
    };

    this.ws.onmessage = (event) => {
      const data = JSON.parse(event.data);
      console.log('Received:', data);
      this.handleMessage(data);
    };

    this.ws.onclose = (event) => {
      console.warn(`Closed: code=${event.code}, reason=${event.reason}`);
      clearInterval(this.pingInterval);
      // 필요 시 지연 후 재연결 시도
      setTimeout(() => this.connect(), 3000);
    };

    this.ws.onerror = (err) => {
      console.error('WebSocket Error:', err);
    };
  }

  send(msgObj) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(msgObj));
    }
  }

  handleMessage(msg) {
    switch (msg.type) {
      case 'SYSTEM_NOTICE':
        console.log('System:', msg.payload);
        break;
      case 'BROADCAST':
        console.log(`[${msg.sender}]: ${msg.payload}`);
        break;
      case 'PONG':
        // Heartbeat ACK
        break;
    }
  }
}

// 사용 예시
const client = new WsClient('ws://localhost/ws/?userId=user1');
client.connect();
```

---

### 5.2 Android (Kotlin + OkHttp)
```kotlin
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class WebSocketManager(private val serverUrl: String) {
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(30, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null

    fun connect() {
        val request = Request.Builder().url(serverUrl).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                println("WS Connected")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val json = JSONObject(text)
                val type = json.optString("type")
                val payload = json.opt("payload")
                println("[$type] $payload")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                System.err.println("WS Failure: ${t.message}")
            }
        })
    }

    fun sendMessage(type: String, payload: Any, sender: String? = null) {
        val json = JSONObject().apply {
            put("type", type)
            put("payload", payload)
            sender?.let { put("sender", it) }
        }
        webSocket?.send(json.toString())
    }
}
```

---

### 5.3 iOS (Swift / URLSessionWebSocketTask)
```swift
import Foundation

class WebSocketClient: NSObject, URLSessionWebSocketDelegate {
    private var webSocketTask: URLSessionWebSocketTask?
    
    func connect(urlString: String) {
        guard let url = URL(string: urlString) else { return }
        let session = URLSession(configuration: .default, delegate: self, delegateQueue: OperationQueue())
        webSocketTask = session.webSocketTask(with: url)
        webSocketTask?.resume()
        receiveMessage()
    }
    
    private func receiveMessage() {
        webSocketTask?.receive { [weak self] result in
            switch result {
            case .success(let message):
                switch message {
                case .string(let text):
                    print("Received string: \(text)")
                default: break
                }
                self?.receiveMessage()
            case .failure(let error):
                print("Error receiving: \(error)")
            }
        }
    }
    
    func send(jsonString: String) {
        let message = URLSessionWebSocketTask.Message.string(jsonString)
        webSocketTask?.send(message) { error in
            if let error = error {
                print("Send error: \(error)")
            }
        }
    }
}
```

---

### 5.4 CLI (터미널 `websocat` 테스트)
```bash
# Nginx 프록시를 통해 연결
websocat ws://localhost/ws/

# 연결 후 JSON 메시지 직접 입력하여 송신:
{"type": "BROADCAST", "sender": "cli-tester", "payload": "CLI에서 보냄"}
{"type": "PING", "payload": "test"}
{"type": "ROOM_JOIN", "sender": "cli-tester", "roomId": "room1"}
```

---

## 6. 연결 생명주기 및 예외 처리 가이드

1. **연결 종료 코드 (Close Code)**:
   - `1000 (NORMAL_CLOSURE)`: 정상적인 세션 종료 (`ws.close()`)
   - `1001 (GOING_AWAY)`: 브라우저 탭 닫힘 / 페이지 새로고침
   - `1006 (ABNORMAL_CLOSURE)`: 네트워크 단절 또는 예기치 않은 프록시 연결 끊김 (클라이언트에서 즉시 감지 후 지수 백오프로 재연결 시도 권장)
2. **재연결 백오프 전략 (Exponential Backoff)**:
   - 네트워크 순단 발생 시 `1초 ➔ 2초 ➔ 4초 ➔ 최대 30초` 간격으로 점진적 재연결을 시도하여 서버 과부하를 방지합니다.
3. **Nginx 타임아웃 대응**:
   - `nginx.conf`의 `proxy_read_timeout`이 86400초(24시간)로 설정되어 있으나, 통신 유휴 방지를 위해 클라이언트는 최소 **30초마다 `PING` 메시지**를 서버에 송신하는 것을 권장합니다.
