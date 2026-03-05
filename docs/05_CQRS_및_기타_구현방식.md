# CQRS 및 기타 구현 방식 (현재 소스 기준)

## 1. 현재 적용 상태 요약
- Clean Architecture: 적용
- CQRS: **적용** (Command/Query 분리)

현재 Product는
- Command: `ProductCommandService`
- Query: `ProductQueryService`
로 나뉘어 동작한다.

---

## 2. CQRS 적용 형태 (현재 코드)
### Command 측
- 생성/수정/삭제 담당
- 도메인 규칙 및 트랜잭션 변경 책임

### Query 측
- 조회 전담
- 읽기 API 응답 최적화 기반

현재는 같은 DB를 사용하지만,
코드 책임은 분리되어 있다(논리적 CQRS).

---

## 3. Event Sourcing과의 관계
- CQRS: 읽기/쓰기 책임 분리
- Event Sourcing: 상태 대신 이벤트 이력 저장

둘은 독립 개념이며, 같이 쓸 수도 있다.
현재 소스는 CQRS만 적용된 상태다.

---

## 4. 언제 더 확장하나?
아래가 필요하면 다음 단계로 간다.
- 조회 성능 병목
- 대량 조회 트래픽
- 읽기 모델을 별도로 최적화해야 하는 요구

그때 선택:
1. Query 전용 테이블/뷰
2. 이벤트 기반 동기화
3. 필요 시 Event Sourcing

---

## 5. 한 줄 정리
현재 소스는
**CQRS를 적용해 Command/Query 책임을 분리한 상태**이며,
추가 복잡도(이벤트/ES)는 필요 시 단계적으로 도입하면 된다.
