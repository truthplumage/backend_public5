# Product 임베딩

이 문서는 상품 임베딩이 어떻게 동작하는지 쉽게 정리한 문서입니다.

## 한 줄로 보면

상품 이름과 설명을 읽어서  
검색용 숫자값으로 바꾸고  
그 값을 `embedding`에 저장합니다.

## 무엇을 읽는가

지금은 아래 두 개만 사용합니다.

- `name`
- `description`

예:

```text
상품명: 맥북 프로 14
설명: M3 칩셋, 16GB RAM, 512GB SSD
```

이 문장을 OpenAI에 보내면 숫자 배열이 돌아옵니다.  
그 숫자 배열이 임베딩입니다.

## 언제 만들어지나

두 경우에 만들어집니다.

1. 상품 생성 또는 수정할 때
2. 전체 재생성 API를 호출할 때

## 저장 흐름

1. 상품 저장
2. `name + description`으로 문장 만들기
3. OpenAI 임베딩 API 호출
4. 결과를 `product.embedding`에 저장

## 검색 흐름

1. 사용자가 문장으로 검색
2. 검색어도 임베딩으로 변환
3. `product.embedding`과 비교
4. 가장 비슷한 상품 반환

## API

벡터 검색:

```http
GET /api/products/semantic-search?query=영상 편집용 노트북&size=5
```

전체 임베딩 재생성:

```http
POST /api/products/embeddings/refresh
```

## 설정

기본값은 꺼져 있습니다.

```yaml
openai:
  embedding:
    enabled: false
```

실제로 돌리려면:

```yaml
openai:
  embedding:
    enabled: true
```

환경변수도 필요합니다.

```bash
OPENAI_API_KEY=...
```

## 꺼져 있으면

- 상품은 저장됩니다
- OpenAI는 호출하지 않습니다
- `embedding`은 비어 있을 수 있습니다

## 관련 파일

- 서비스: [ProductEmbeddingService.java](/Users/parkjinwoo/source/study/grepp-BE5/backend5/src/main/java/com/grepp/backend5/product/application/vector/ProductEmbeddingService.java)
- OpenAI 구현: [OpenAiProductEmbeddingGenerator.java](/Users/parkjinwoo/source/study/grepp-BE5/backend5/src/main/java/com/grepp/backend5/product/infrastructure/vector/OpenAiProductEmbeddingGenerator.java)
- 비활성 구현: [NoOpProductEmbeddingGenerator.java](/Users/parkjinwoo/source/study/grepp-BE5/backend5/src/main/java/com/grepp/backend5/product/infrastructure/vector/NoOpProductEmbeddingGenerator.java)
- 상품 서비스: [ProductApplicationService.java](/Users/parkjinwoo/source/study/grepp-BE5/backend5/src/main/java/com/grepp/backend5/product/application/service/ProductApplicationService.java)
- 상품 API: [ProductController.java](/Users/parkjinwoo/source/study/grepp-BE5/backend5/src/main/java/com/grepp/backend5/product/presentation/controller/ProductController.java)

## 지금 기억할 것

- 임베딩은 검색용 숫자값입니다
- 원본은 `name + description`입니다
- OpenAI를 켜야 실제로 생성됩니다
