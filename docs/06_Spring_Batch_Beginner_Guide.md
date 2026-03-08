# Spring Batch 입문 가이드 (현재 소스 기준)

## 1. 배치(Batch)란?
배치는 "많은 데이터를 정해진 규칙으로 한 번에 처리"하는 방식입니다.

- 예시: 하루치 정산, 대량 데이터 집계, 엑셀/CSV 일괄 처리
- 장점: 재시도, 실행 이력 관리, 단계별 처리 가능

이 프로젝트에서는 Spring Batch로 정산 예시를 구성했습니다.

## 2. 현재 코드 구조

### 배치 핵심 클래스
- `Job/Step 설정`: `src/main/java/com/grepp/backend5/batch/config/SettlementBatchConfig.java`
- `Tasklet 로직`: `src/main/java/com/grepp/backend5/batch/job/SettlementTasklet.java`
- `Job 실행 서비스`: `src/main/java/com/grepp/backend5/batch/service/SettlementJobLauncher.java`
- `실행 API`: `src/main/java/com/grepp/backend5/batch/presentation/BatchJobController.java`

### 현재 제공되는 Job
1. `settlementJob` (Tasklet 방식)
- Step: `settlementStep`
- Tasklet: `SettlementTasklet`

2. `settlementChunkJob` (Chunk 방식)
- Step: `settlementChunkStep`
- Reader/Processor/Writer 사용

## 3. Job / Step / Tasklet / Chunk 쉽게 이해하기

### Job
배치 작업 전체 단위입니다.

### Step
Job 안의 작업 단계입니다.
- Job 하나에 Step 여러 개를 붙일 수 있습니다.

### Tasklet
Step에서 "한 번에" 처리하는 방식입니다.
- 단순 로직, 짧은 작업에 적합

### Chunk
Step에서 "읽기-가공-쓰기"를 묶음으로 반복 처리합니다.
- 대용량 처리에 유리
- 이 프로젝트는 `chunk(3)`으로 샘플 구성되어 있습니다.

## 4. 현재 실행 방식

### 왜 자동 실행을 껐는가?
`application.yaml`에서 아래처럼 설정되어 있습니다.

```yaml
spring:
  batch:
    job:
      enabled: false
```

의미:
- 서버 시작할 때 Job 자동 실행하지 않음
- API 호출로 원하는 시점에 수동 실행

## 5. 실행 API

### 1) Tasklet Job 실행
- `POST /api/batch/jobs/settlement`
- 쿼리 파라미터: `settlementDate` (선택, 형식 `yyyy-MM-dd`)

예시:
```bash
curl -X POST "http://localhost:8080/api/batch/jobs/settlement?settlementDate=2026-03-09"
```

### 2) Chunk Job 실행
- `POST /api/batch/jobs/settlement-chunk`
- 쿼리 파라미터: `settlementDate` (선택, 형식 `yyyy-MM-dd`)

예시:
```bash
curl -X POST "http://localhost:8080/api/batch/jobs/settlement-chunk?settlementDate=2026-03-09"
```

## 6. 배치 메타 테이블
Spring Batch는 실행 이력을 DB 테이블로 관리합니다.

현재 프로젝트는 PostgreSQL 기준으로 자동 생성되도록 설정되어 있습니다.

```yaml
spring:
  sql:
    init:
      mode: always
      schema-locations: classpath:org/springframework/batch/core/schema-postgresql.sql
      continue-on-error: true
```

대표 테이블:
- `batch_job_instance`
- `batch_job_execution`
- `batch_job_execution_params`
- `batch_step_execution`

## 7. 학습 순서
1. `settlementJob`(Tasklet) 흐름 먼저 이해
2. `settlementChunkJob`에서 Reader/Processor/Writer 역할 분리 이해
3. 샘플 Reader/Writer를 실제 DB 조회/저장 로직으로 교체
4. Step을 2개 이상으로 늘려서 검증 단계 + 저장 단계 분리

## 8. 다음 확장 포인트
- `ItemReader`를 JPA/JDBC 기반으로 변경
- 실패 데이터 재처리(fault tolerant) 설정
- 스케줄러(`@Scheduled` 또는 외부 Cron)와 연동
- 운영 환경에서 배치 전용 실행 프로필 분리
