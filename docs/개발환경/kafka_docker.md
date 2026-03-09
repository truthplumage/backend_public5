# Kafka Docker 실행 방법

- Kafka 주소: `localhost:9092`
- 토픽 이름: `async-orders`
- Consumer Group: `async-sample-group`

## 1. Kafka 이미지 받기

```bash
docker pull apache/kafka:4.1.1
```

## 2. Kafka 컨테이너 실행

```bash
docker run -d \
  --name backend5-kafka \
  -p 9092:9092 \
  apache/kafka:4.1.1
```

설명:

- `--name backend5-kafka`: 컨테이너 이름 지정
- `-p 9092:9092`: 호스트 `9092` 포트를 Kafka `9092` 포트와 연결

이 프로젝트는 `application.yaml`에서 `localhost:9092`로 Kafka에 연결합니다.  
그래서 포트 매핑이 반드시 맞아야 합니다.

## 3. 실행 확인

컨테이너 실행 상태 확인:

```bash
docker ps
```

정상 예시:

```bash
CONTAINER ID   IMAGE                COMMAND                  STATUS         PORTS
xxxxxxx        apache/kafka:4.1.1   "/__cacert_entrypoin…"   Up 10 seconds  0.0.0.0:9092->9092/tcp
```

포트 리슨 확인:

```bash
lsof -iTCP:9092 -sTCP:LISTEN -n -P
```

## 4. Kafka 내부 확인

토픽 목록 조회:

```bash
docker exec backend5-kafka bash -lc '/opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list'
```

지금은 토픽이 없으면 아무것도 출력되지 않을 수 있습니다.

## 5. 중지 / 삭제

중지:

```bash
docker stop backend5-kafka
```

다시 시작:

```bash
docker start backend5-kafka
```

삭제:

```bash
docker rm -f backend5-kafka
```

## 6. 빠른 실행 순서

1. Kafka 이미지 받기
2. Kafka 컨테이너 실행
3. `docker ps`로 포트 확인
4. Spring Boot 실행
5. Kafka 관련 API 또는 Listener 동작 확인

현재 프로젝트에서는 Kafka가 켜져 있으면 애플리케이션 시작 시 컨슈머가 자동으로 연결을 시도합니다.

## 7. Windows

핵심은 Docker Desktop이 실행 중이어야 하고, `9092` 포트를 Kafka에 연결하는 것입니다.

### 1) Kafka 이미지 받기

PowerShell 또는 CMD:

```powershell
docker pull apache/kafka:4.1.1
```

### 2) Kafka 컨테이너 실행

PowerShell:

```powershell
docker run -d `
  --name backend5-kafka `
  -p 9092:9092 `
  apache/kafka:4.1.1
```

CMD:

```cmd
docker run -d ^
  --name backend5-kafka ^
  -p 9092:9092 ^
  apache/kafka:4.1.1
```

한 줄로 실행해도 됩니다.

```powershell
docker run -d --name backend5-kafka -p 9092:9092 apache/kafka:4.1.1
```

### 3) 실행 확인

```powershell
docker ps
```

포트 확인:

```powershell
netstat -ano | findstr 9092
```

정상이라면 `0.0.0.0:9092` 또는 `127.0.0.1:9092` 같은 내용이 보일 수 있습니다.

### 4) Kafka 내부 확인

```powershell
docker exec backend5-kafka bash -lc "/opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list"
```

### 5) 중지 / 시작 / 삭제

중지:

```powershell
docker stop backend5-kafka
```

다시 시작:

```powershell
docker start backend5-kafka
```

삭제:

```powershell
docker rm -f backend5-kafka
```

### 6) 자주 생기는 문제

- `docker` 명령이 안 잡히면 Docker Desktop이 실행 중인지 확인
- `port is already allocated`가 나오면 `9092` 포트를 다른 프로세스가 사용 중인지 확인
- Spring Boot가 Kafka에 연결되지 않으면 프로젝트 설정이 `localhost:9092`인지 확인
