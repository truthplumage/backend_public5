# Tasklet

## 1. Tasklet이란?
Tasklet은 Spring Batch에서 "Step 한 번 실행 시 한 번 처리"하는 방식입니다.

쉽게 말하면:
- Job(배치 작업) 안에 Step(단계)이 있고
- 그 Step 안에서 Tasklet 코드가 실행됩니다.

현재 프로젝트는 정산 예시로 Tasklet을 사용 중입니다.

## 2. 현재 코드에서 어디를 보면 되나?

- Tasklet 구현: `SettlementTasklet.java`
- Job/Step 연결: `SettlementBatchConfig.java`
- 실행 서비스: `SettlementJobLauncher.java`
- 실행 API: `BatchJobController.java`

핵심 연결 구조:
1. `settlementJob`
2. `settlementStep`
3. `SettlementTasklet.execute(...)`

즉, `Job -> Step -> Tasklet` 순서로 동작합니다.

## 3. Tasklet 실행 흐름

1. API 호출
- `POST /api/batch/jobs/settlement?settlementDate=2026-03-09`

2. `BatchJobController`에서 런처 호출

3. `SettlementJobLauncher`가 `settlementJob` 실행

4. `settlementStep`에서 `SettlementTasklet.execute(...)` 실행

5. 로그 출력 후 `RepeatStatus.FINISHED` 반환

## 4. Tasklet 메서드에서 하는 일

`SettlementTasklet.execute(...)`에서 현재 하는 작업:
- Job 파라미터 `settlementDate` 읽기
- 날짜 형식 검증 (`yyyy-MM-dd`)
- 시작/종료 로그 출력
- 완료 상태 반환

지금은 샘플 로직이고, 여기에 실제 정산 집계/저장 로직을 넣으면 됩니다.

## 5. Tasklet이 좋은 경우

- 처리 로직이 단순할 때
- 한 번에 끝나는 작업일 때
- 단계별 제어가 필요한 배치일 때

예시:
- 하루치 파일 1개 읽고 DB 반영
- 특정 상태 데이터 일괄 업데이트

## 6. Chunk와 차이

- Tasklet: 한 번 실행해서 끝내는 방식
- Chunk: Reader/Processor/Writer로 대량 데이터를 묶음 처리

정산처럼 데이터가 많아지면 Chunk가 더 유리할 수 있습니다.

## 7. 처음 수정할 때 추천 포인트

1. `SettlementTasklet`에 실제 서비스 호출 추가
2. 실패 시 예외 메시지를 명확히 남기기
3. 로그에 기준일(`settlementDate`)과 처리 건수 남기기
4. 처리 결과를 별도 테이블에 기록하기

## 8. 실행/확인 빠른 명령

서버 실행:
```bash
./gradlew bootRun
```

Tasklet Job 실행:
```bash
curl -X POST "http://localhost:8080/api/batch/jobs/settlement?settlementDate=2026-03-09"
```

결과는 `batch_job_execution`, `batch_step_execution` 테이블에서 확인할 수 있습니다.
