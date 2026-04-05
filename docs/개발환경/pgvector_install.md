# 상품 pgvector 준비 가이드 (macOS / Windows)

이 문서는 현재 `product` 기능에 `pgvector`를 붙이기 위해 필요한 준비 순서를 정리한 문서입니다.

현재 구조는 이렇게 봅니다.

- 상품 원본 데이터: `product` 테이블
- 상품 임베딩 데이터: `product_embedding` 테이블
- 벡터 검색 대상: `product_embedding.embedding`

즉 `pgvector`는 상품 자체를 저장하는 것이 아니라, **상품을 벡터로 변환한 값**을 저장할 때 필요합니다.

---

## 먼저 이해할 것

현재 프로젝트에서 필요한 것은 두 가지입니다.

1. PostgreSQL이 `vector` 타입을 알아야 함
2. `product_embedding` 테이블을 만들 수 있어야 함

그래서 순서는 항상 같습니다.

1. PostgreSQL 준비
2. `pgvector` 설치
3. `CREATE EXTENSION vector`
4. 애플리케이션 실행
5. `product_embedding` 테이블 확인

---

## macOS (Homebrew)

```bash
# 0) pgvector 설치
brew install pgvector

# 1) PostgreSQL 재시작
brew services restart postgresql@18

# 2) 접속
psql -d backend5
# 또는
psql -U $(whoami) -d backend5
```

접속 후 실행:

```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

확인:

```sql
\dx
SELECT '[1,2,3]'::vector;
```

여기까지 되면 `vector` 타입을 사용할 준비가 끝납니다.

---

## Windows

Windows에서는 Docker 방식이 가장 단순합니다.

### Docker로 PostgreSQL + pgvector 실행

PowerShell:

```powershell
docker run -d `
  --name backend5-pgvector `
  -e POSTGRES_PASSWORD=postgres `
  -e POSTGRES_DB=backend5 `
  -p 5432:5432 `
  pgvector/pgvector:pg18
```

접속:

```powershell
docker exec -it backend5-pgvector psql -U postgres -d backend5
```

접속 후 실행:

```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

확인:

```sql
\dx
SELECT '[1,2,3]'::vector;
```

---

## Windows에서 PostgreSQL을 이미 설치한 경우

이미 PostgreSQL이 설치되어 있어도 `pgvector` 확장 파일이 없으면 바로 사용할 수 없습니다.

먼저 `SQL Shell (psql)` 또는 `psql`에 접속해서 아래를 실행합니다.

```sql
CREATE EXTENSION vector;
```

정상이라면:

```sql
CREATE EXTENSION
```

반대로 아래 오류가 나오면:

```sql
extension "vector" is not available
```

이 의미는 현재 PostgreSQL에 `pgvector`가 설치되어 있지 않다는 뜻입니다.  
이 경우는 Docker 방식으로 가는 편이 가장 빠릅니다.

---

## 현재 프로젝트에서 실제로 쓰는 테이블

이 프로젝트는 아래 SQL 파일을 사용합니다.

- [pgvector.sql](/Users/parkjinwoo/source/study/grepp-BE5/backend5/src/main/resources/db/pgvector.sql)

이 파일은 아래 테이블을 만듭니다.

```sql
CREATE TABLE IF NOT EXISTS public.product_embedding (
    product_id UUID PRIMARY KEY REFERENCES public."product"(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    embedding VECTOR(1536) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
```

의미:

- `product_id`
  - 원본 상품과 연결되는 키
- `content`
  - 임베딩 생성에 사용한 상품 텍스트
- `embedding`
  - OpenAI 임베딩 벡터 값
- `updated_at`
  - 마지막 동기화 시각

즉 상품은 `product`에 저장되고, 검색용 벡터는 `product_embedding`에 따로 저장됩니다.

---

## 실행 후 확인할 것

애플리케이션 실행 후 아래를 확인하면 됩니다.

### 1) 확장 확인

```sql
\dx
```

목록에 `vector`가 있어야 합니다.

### 2) 테이블 확인

```sql
\d public.product_embedding
```

여기서 `embedding` 컬럼 타입이 `vector(1536)`이면 정상입니다.

### 3) 데이터 확인

```sql
SELECT product_id, updated_at
FROM public.product_embedding
LIMIT 5;
```

값이 들어 있으면 상품 임베딩 저장까지 된 상태입니다.

---

## 상품 기준으로 보면 최종 흐름은 이렇습니다

1. 상품 생성 또는 수정
2. 상품 정보를 하나의 텍스트로 만듦
3. OpenAI 임베딩 API 호출
4. 임베딩 값을 `product_embedding`에 저장
5. 사용자 질문이 들어오면 다시 임베딩 생성
6. `product_embedding.embedding`과 유사도 검색

즉 `pgvector`는 상품 벡터 검색을 위해 들어가는 저장소 기능입니다.

---

## 자주 생기는 오류

- `type "vector" does not exist`

```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

- `extension "vector" is not available`

의미:
- PostgreSQL 서버에 `pgvector`가 설치되지 않은 상태입니다.

해결:
- macOS면 `brew install pgvector`
- Windows면 Docker에서 `pgvector/pgvector:pg18` 사용

- `relation "product_embedding" does not exist`

의미:
- 확장은 준비됐지만 테이블 생성이 아직 안 된 상태입니다.

확인:
- 애플리케이션이 `src/main/resources/db/pgvector.sql`을 실행했는지 확인

- `psql: command not found`

해결:
- [postgres18_brew.md](/Users/parkjinwoo/source/study/grepp-BE5/backend5/docs/개발환경/postgres18_brew.md)의 PATH 설정 부분을 먼저 적용합니다.

---

종료는 `\q` 입니다.
