# Chunk

## 1. Chunk란?
Chunk는 Spring Batch에서 데이터를 `읽기 -> 가공 -> 쓰기`로 나누어 묶음 단위로 처리하는 방식입니다.

구성 요소:
- ItemReader: 데이터 읽기
- ItemProcessor: 데이터 변환/가공
- ItemWriter: 데이터 저장

현재 프로젝트는 정산 예시로 Chunk를 사용 중입니다.

## 2. 현재 코드에서 어디를 보면 되나?

- Chunk Job/Step 설정: `SettlementBatchConfig.java`
- 실행 서비스: `SettlementJobLauncher.java`
- 실행 API: `BatchJobController.java`

핵심 연결 구조:
1. `settlementChunkJob`
2. `settlementChunkStep`
3. `settlementChunkReader` -> `settlementChunkProcessor` -> `settlementChunkWriter`

즉, `Job -> Step -> Reader/Processor/Writer` 순서로 동작합니다.

## 3. Chunk 실행 흐름

1. API 호출
- `POST /api/batch/jobs/settlement-chunk?settlementDate=2026-03-09`

2. `BatchJobController`에서 런처 호출

3. `SettlementJobLauncher`가 `settlementChunkJob` 실행

4. `settlementChunkStep`에서 Chunk 처리 실행
- reader가 아이템 읽음
- processor가 아이템 변환
- writer가 결과 저장(현재는 로그 출력)

## 4. 현재 Chunk 설정

`SettlementBatchConfig`에서 현재 샘플 설정:
- chunk size: `3`
- reader 입력 예시: `1001, 1002, 1003, 1004, 1005`
- processor 결과: `SETTLEMENT_LINE(...)` 문자열
- writer 동작: 로그 출력

이 값은 샘플이며 실제 정산 로직에 맞게 교체하면 됩니다.

## 5. Chunk가 유리한 경우

- 처리 대상 데이터가 많을 때
- 읽기/가공/쓰기 책임을 분리하고 싶을 때
- 중간 실패 시 재시도/재처리 전략이 필요할 때

예시:
- 일별 주문 데이터 정산 집계
- 대량 회원 포인트 정산

## 6. Tasklet과 차이

- Tasklet: 한 번 실행 후 종료되는 단일 작업
- Chunk: 대량 데이터를 반복적으로 분할 처리

데이터 처리량이 커질수록 Chunk 구조가 일반적으로 더 적합합니다.

## 7. 수정할 때 우선 포인트

1. `ItemReader`를 실제 DB 조회(JDBC/JPA)로 교체
2. `ItemProcessor`에 정산 계산식(수수료/순액) 반영
3. `ItemWriter`를 DB 저장/업서트 로직으로 교체
4. Chunk 크기, 트랜잭션, 실패 재시도 정책 조정

## 8. 실행/확인 명령

서버 실행:
```bash
./gradlew bootRun
```

Chunk Job 실행:
```bash
curl -X POST "http://localhost:8080/api/batch/jobs/settlement-chunk?settlementDate=2026-03-09"
```

결과는 `batch_job_execution`, `batch_step_execution` 테이블에서 확인할 수 있습니다.
