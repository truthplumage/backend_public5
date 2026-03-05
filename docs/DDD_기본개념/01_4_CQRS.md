# CQRS (Command Query Responsibility Segregation) - 현재 소스 기준

## 1. 한 줄 정의
CQRS는 쓰기(Command)와 읽기(Query) 책임을 분리하는 방식이다.

- Command: 생성/수정/삭제
- Query: 조회

---

## 2. 현재 프로젝트
분리 포인트:
- `ProductCommandUseCase` / `ProductQueryUseCase` 분리
- `ProductCommandService` / `ProductQueryService` 분리
- 저장소도 command/query로 분리
---

## 3. 실제 코드 위치
### UseCase
- `src/main/java/com/grepp/backend5/product/application/command/usecase/ProductCommandUseCase.java`
- `src/main/java/com/grepp/backend5/product/application/query/usecase/ProductQueryUseCase.java`

### Service
- `src/main/java/com/grepp/backend5/product/application/command/service/ProductCommandService.java`
- `src/main/java/com/grepp/backend5/product/application/query/service/ProductQueryService.java`

### Repository
- `src/main/java/com/grepp/backend5/product/domain/repository/command/ProductCommandRepository.java`
- `src/main/java/com/grepp/backend5/product/domain/repository/query/ProductQueryRepository.java`
- `src/main/java/com/grepp/backend5/product/infrastructure/persistence/command/ProductCommandRepositoryAdapter.java`
- `src/main/java/com/grepp/backend5/product/infrastructure/persistence/query/ProductQueryRepositoryAdapter.java`

### Controller
- `src/main/java/com/grepp/backend5/product/presentation/controller/ProductController.java`

---

## 4. 현재 소스에서 CQRS가 동작하는 방식
1. Controller가 요청을 받음
2. 생성/수정/삭제는 `ProductCommandUseCase` 호출
3. 조회는 `ProductQueryUseCase` 호출
4. 두 서비스는 서로 책임을 섞지 않음

핵심: 클래스가 늘어난 대신 책임이 명확해짐

---

## 5. 장단점
### 장점
- 읽기/쓰기 코드가 섞이지 않아 이해하기 쉬움
- 조회 로직 최적화 시 쓰기 규칙에 영향이 적음

### 단점
- 파일/인터페이스 수가 늘어남
- 작은 프로젝트에는 과할 수 있음
