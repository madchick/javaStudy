# 🚀 Server Stack (Linux, Redis, Kafka, PostgreSQL, Nginx)

도커 기반으로 구성된 서버 스택 환경입니다.

## 📦 컨테이너 구성 및 포트 정보

| 서비스 | 컨테이너 이름 | 내부 포트 | 호스트 외부 포트 | 접속 정보 / 계정 |
|---|---|---|---|---|
| **Nginx** | `my-nginx` | 80 | `http://localhost:80` | 웹 대시보드 및 리버스 프록시 |
| **PostgreSQL** | `my-postgres` | 5432 | `localhost:5432` | user: `postgres` / pw: `postgres` / db: `testdb` |
| **Redis** | `my-redis` | 6379 | `localhost:6379` | 암호 없음 |
| **Kafka** | `my-kafka` | 9092 | `localhost:9092` | KRaft 모드 (Zookeeper 불필요) |
| **Linux (Ubuntu)** | `my-linux` | - | - | 공용 이미지 `linux-ubuntu` 사용 (curl, psql, redis-cli, websocat 등 내장) |

---

## 🔌 외부 툴 및 애플리케이션 접속 정보

내 노트북(호스트)의 DB 툴(DBeaver, DataGrip 등)이나 백엔드(Spring Boot)에서 컨테이너로 접속할 때 사용하는 정보입니다.

### 🐘 PostgreSQL (DBeaver 접속 정보)
| 항목 | 설정값 |
|---|---|
| **Database Type** | **PostgreSQL** |
| **Host** | `localhost` *(또는 `127.0.0.1`)* |
| **Port** | `5432` |
| **Database** | `testdb` *(또는 기본 DB `postgres`)* |
| **Username** | `postgres` |
| **Password** | `postgres` |

### ⚡ Redis (Redis Insight / CLI)
| 항목 | 설정값 |
|---|---|
| **Host** | `localhost` |
| **Port** | `6379` |
| **Password** | *(없음)* |

### 📨 Kafka (Spring Boot / Kafka GUI Tool)
| 항목 | 설정값 |
|---|---|
| **Bootstrap Server** | `localhost:9092` |

---

## 📁 주요 파일 및 웹 리소스 경로

| 구분 | 호스트 (내 PC) 경로 | 컨테이너 내부 마운트 경로 | 설명 |
|---|---|---|---|
| **Nginx Web Root** | [`./nginx/html/index.html`](file:///c:/Projects/javaStudy/serverStackTest/nginx/html/index.html) | `/usr/share/nginx/html/index.html` | 웹 브라우저(`http://localhost`) 접속 시 노출되는 메인 페이지 |
| **Nginx 설정** | [`./nginx/nginx.conf`](file:///c:/Projects/javaStudy/serverStackTest/nginx/nginx.conf) | `/etc/nginx/nginx.conf` | Nginx 가상 호스트 및 프록시 설정 파일 |
| **Linux Dockerfile** | [`./linux/Dockerfile`](file:///c:/Projects/javaStudy/serverStackTest/linux/Dockerfile) | - | Ubuntu 유틸리티 컨테이너 빌드 정의서 |
| **도커 컴포즈** | [`./docker-compose.yml`](file:///c:/Projects/javaStudy/serverStackTest/docker-compose.yml) | - | 전체 5개 컨테이너 통합 설정 파일 |

---

## 🛠️ 주요 명령어 안내

### 1. 스택 시작 및 종료
```bash
# 백그라운드 실행
docker compose up -d

# 상태 확인
docker compose ps

# 전체 중지 및 종료
docker compose down
```

### 2. Linux 유틸 컨테이너 진입
```bash
docker exec -it my-linux bash
```
> `my-linux` 내부에서는 `postgres`, `redis`, `kafka`, `nginx`와 같은 서비스 이름(도메인)으로 바로 접근 가능합니다.  
> 예시:
> - `redis-cli -h redis ping`
> - `psql -h postgres -U postgres -d testdb`
> - `curl http://nginx`

### 3. PostgreSQL 직접 접속
```bash
docker exec -it my-postgres psql -U postgres -d testdb
```

### 4. Redis CLI 직접 접속
```bash
docker exec -it my-redis redis-cli
```

### 5. Kafka 토픽 생성 및 테스트
```bash
# 토픽 생성
docker exec -it my-kafka /opt/kafka/bin/kafka-topics.sh --create --topic test-topic --bootstrap-server localhost:9092

# 토픽 목록 확인
docker exec -it my-kafka /opt/kafka/bin/kafka-topics.sh --list --bootstrap-server localhost:9092

# 메시지 프로듀서 실행
docker exec -it my-kafka /opt/kafka/bin/kafka-console-producer.sh --topic test-topic --bootstrap-server localhost:9092

# 메시지 컨슈머 실행
docker exec -it my-kafka /opt/kafka/bin/kafka-console-consumer.sh --topic test-topic --from-beginning --bootstrap-server localhost:9092
```
