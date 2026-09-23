FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y \
    curl \
    wget \
    iputils-ping \
    net-tools \
    dnsutils \
    netcat-openbsd \
    socat \
    vim \
    nano \
    git \
    unzip \
    redis-tools \
    postgresql-client \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# WebSocket CLI 테스트 도구 (websocat) 설치
RUN ARCH=$(uname -m) && \
    ( \
      if [ "$ARCH" = "x86_64" ]; then \
        wget -q https://github.com/vi/websocat/releases/latest/download/websocat.x86_64-unknown-linux-musl -O /usr/local/bin/websocat && \
        chmod +x /usr/local/bin/websocat; \
      elif [ "$ARCH" = "aarch64" ]; then \
        wget -q https://github.com/vi/websocat/releases/latest/download/websocat.aarch64-unknown-linux-musl -O /usr/local/bin/websocat && \
        chmod +x /usr/local/bin/websocat; \
      fi \
    ) || true

WORKDIR /root

CMD ["bash"]
