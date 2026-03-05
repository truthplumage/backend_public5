# 이벤트 기반 통합 (Event-Driven Integration) - 현재 소스 기준

## 1. 한 줄 정의
현재 프로젝트의 이벤트 기반은
**Spring Application Event를 사용한 내부(in-process) 발행/구독 방식**이다.

즉, 같은 애플리케이션 안에서
- 서비스가 이벤트를 발행하고
- 핸들러가 이벤트를 구독해 후속 처리를 한다.

---

## 2. 현재 프로젝트 상태 (중요)
현재 `product` 모듈은 이벤트 기반 처리 **적용 상태**다.

적용된 요소:
- 이벤트 발행: `ProductApplicationService`
- 이벤트 타입: `ProductCreatedEvent`, `ProductUpdatedEvent`, `ProductDeletedEvent`
- 이벤트 구독: `ProductEventHandler`
- 구독 시점: `@TransactionalEventListener(phase = AFTER_COMMIT)`

주의:
- Kafka/RabbitMQ 같은 외부 브로커는 **현재 사용하지 않는다**.
- 분산 시스템 이벤트가 아니라 애플리케이션 내부 이벤트다.

---

## 3. 실제 코드 위치
### 이벤트 발행
- `src/main/java/com/grepp/backend5/product/application/service/ProductApplicationService.java`

### 이벤트 타입
- `src/main/java/com/grepp/backend5/product/application/event/ProductCreatedEvent.java`
- `src/main/java/com/grepp/backend5/product/application/event/ProductUpdatedEvent.java`
- `src/main/java/com/grepp/backend5/product/application/event/ProductDeletedEvent.java`

### 이벤트 구독
- `src/main/java/com/grepp/backend5/product/infrastructure/event/ProductEventHandler.java`

### 테스트
- `src/test/java/com/grepp/backend5/product/application/service/ProductApplicationServiceTest.java`

---

## 4. 현재 소스의 이벤트 흐름
1. `create/update/delete` 실행
2. 서비스가 `ApplicationEventPublisher.publishEvent(...)` 호출
3. DB 트랜잭션 커밋 성공
4. `ProductEventHandler`가 `AFTER_COMMIT`으로 이벤트 수신
5. 현재는 로그 후속 처리

핵심:
- 커밋 전에 실패하면 이벤트 핸들러가 실행되지 않는다.
- 데이터가 확정된 후에만 후속 작업을 태울 수 있다.

---

## 5. 장단점
### 장점
- 서비스 코드와 후속 처리 코드를 분리하기 쉽다.
- 같은 앱 내부에서는 구현이 단순하다.
- 트랜잭션 커밋 이후 실행 시점을 명확히 제어할 수 있다.

### 단점
- 외부 서비스로 이벤트를 전달하지는 못한다.
- 프로세스가 내려가면 브로커처럼 메시지 내구성을 기대하기 어렵다.
- 운영 확장(재처리/DLQ)은 별도 설계가 필요하다.

---

## 6. 자주 하는 실수
1. 커밋 이전에 후속 처리 가정
   - `AFTER_COMMIT`이면 커밋 성공 후에만 실행된다.
2. 내부 이벤트와 분산 이벤트를 같은 수준으로 이해
   - 현재는 내부 이벤트다.
3. 이벤트 이름을 기술 용어로 작성
   - `ProductCreated`처럼 비즈니스 용어를 유지하는 것이 좋다.

---

## 7. 다음 단계(필요 시)
이벤트를 다른 서비스까지 보내야 하면 다음을 검토한다.
1. Outbox 패턴 도입
2. 메시지 브로커(Kafka/RabbitMQ) 연동
3. 재처리/멱등성/장애 운영 정책 추가

---

## 8. 요약
현재 소스의 이벤트 기반은
**Spring 내부 이벤트 + AFTER_COMMIT 구독** 방식이다.

지금 단계에서는
서비스 결합을 낮추고 후속 처리를 분리하는 데 충분히 유용하다.
