# 이벤트 기반 통합 (Event-Driven Integration) - 현재 소스 기준

## 1. 현재 상태
현재 `product` 소스는 **이벤트 기반 미적용** 상태입니다.

현재 구조:
- `ProductApplicationService`가 직접 저장소를 호출
- `ApplicationEventPublisher` 사용 없음
- `@TransactionalEventListener` 핸들러 없음

---

## 2. 대신 현재 적용된 것
현재는 이벤트보다 **ACL(Anti-Corruption Layer)** 이 적용되어 있습니다.

- `SellerAcl` (Application 포트)
- `SellerAclAdapter` (Infrastructure 어댑터)
- 외부 Seller 응답을 내부 모델(`SellerIdentity`)로 번역

즉,
- 이벤트 기반: 아직 안 씀
- 외부 연동 경계 보호(ACL): 사용 중

---

## 3. 이벤트 기반을 도입하면 달라지는 점
현재 `create/update/delete` 후 후속 작업이 필요해지면,
다음처럼 분리할 수 있습니다.

1. 서비스에서 이벤트 발행
2. 핸들러에서 후속 처리(로그, 알림, 프로젝션 등)
3. 필요 시 `AFTER_COMMIT`로 커밋 후 실행 보장

---

## 4. 체크포인트
1. 지금은 이벤트보다 ACL 경계부터 안정화한다.
2. 후속 작업이 늘어나서 서비스가 비대해질 때 이벤트를 도입한다.
3. 내부 이벤트로 시작하고, 필요해지면 브로커 연동을 검토한다.

---

## 5. 한 줄 요약
현재 소스는 이벤트 기반이 아니라,
**단일 서비스 + ACL 적용 구조**입니다.
