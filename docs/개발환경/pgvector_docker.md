# pgvector Docker 실행 가이드

이 문서는 Docker로 `pgvector`가 포함된 PostgreSQL을 실행하는 방법을 정리한 문서입니다.

현재 프로젝트 기준으로 보면:

- 데이터베이스 이름: `backend5`
- 상품 원본 테이블: `product`
- 상품 임베딩 테이블: `product_embedding`

즉 Docker PostgreSQL 안에서 `vector` 확장을 켜고, 그 안에 `product_embedding` 테이블을 만들면 됩니다.

---

## 1. 컨테이너 실행

```bash
docker run -d \
  --name backend5-pgvector \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=backend5 \
  -p 5432:5432 \
  pgvector/pgvector:pg18
```

설명:

- 컨테이너 이름: `backend5-pgvector`
- DB 이름: `backend5`
- 계정: `postgres`
- 비밀번호: `postgres`
- 포트: `5432`

---

## 2. 실행 확인

```bash
docker ps
```

목록에 `backend5-pgvector`가 보이면 정상입니다.

---

## 3. PostgreSQL 접속

```bash
docker exec -it backend5-pgvector psql -U postgres -d backend5
```

접속되면 `psql` 화면으로 들어갑니다.

---

## 4. vector 확장 활성화

```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

확인:

```sql
\dx
```

목록에 `vector`가 보이면 정상입니다.

간단 확인:

```sql
SELECT '[1,2,3]'::vector;
```

오류 없이 나오면 `pgvector`는 사용할 준비가 끝난 상태입니다.

---

## 5. 현재 프로젝트 스키마 적용

현재 프로젝트에는 아래 SQL 파일이 있습니다.

- [pgvector.sql](/Users/parkjinwoo/source/study/grepp-BE5/backend5/src/main/resources/db/pgvector.sql)

이 파일은:

- `vector` 확장 생성
- `product_embedding` 테이블 생성

을 처리합니다.

애플리케이션을 실행해서 자동 적용해도 되고, 직접 넣어도 됩니다.

직접 넣으려면:

```bash
docker exec -i backend5-pgvector psql -U postgres -d backend5 -f - < src/main/resources/db/pgvector.sql
```

주의:

- 이 SQL은 `product_embedding`이 `product` 테이블을 참조합니다.
- 따라서 `product` 테이블이 먼저 있어야 합니다.
- 보통은 애플리케이션을 한 번 실행해서 `product` 테이블이 만들어진 뒤 적용하면 됩니다.

---

## 6. 테이블 확인

`psql`에서:

```sql
\d public.product_embedding
```

정상이라면 아래 컬럼들을 볼 수 있습니다.

- `product_id`
- `content`
- `embedding`
- `updated_at`

여기서 `embedding` 타입이 `vector(1536)`이면 정상입니다.

---

## 7. 데이터 확인

```sql
SELECT product_id, updated_at
FROM public.product_embedding
LIMIT 5;
```

값이 보이면 상품 임베딩이 저장된 상태입니다.

---

## 8. 프로젝트 기준 동작 흐름

1. 상품 생성 또는 수정
2. 상품 정보를 텍스트로 만듦
3. OpenAI 임베딩 생성
4. `product_embedding.embedding`에 저장
5. 질문이 들어오면 질문도 임베딩 생성
6. `product_embedding` 기준 유사도 검색

즉 Docker PostgreSQL은 상품 벡터 검색용 저장소 역할을 합니다.

---

## 9. 자주 쓰는 명령

컨테이너 시작:

```bash
docker start backend5-pgvector
```

컨테이너 중지:

```bash
docker stop backend5-pgvector
```

로그 확인:

```bash
docker logs backend5-pgvector
```

컨테이너 삭제:

```bash
docker rm -f backend5-pgvector
```

---

## 10. 빠른 순서

1. Docker 컨테이너 실행
2. `psql` 접속
3. `CREATE EXTENSION IF NOT EXISTS vector;`
4. `\dx`
5. `SELECT '[1,2,3]'::vector;`
6. `product` 테이블 생성 확인
7. `pgvector.sql` 적용 또는 앱 실행
8. `product_embedding` 테이블 확인

여기까지 되면 Docker 기준 `pgvector` 준비는 끝입니다.
