# Product 임베딩 생성

이 문서는 현재 `product` 저장 시 벡터가 어떻게 만들어지는지 정리한 문서입니다.

## 한 줄로 보면

상품이 저장되면  
`name + description`을 하나의 문장으로 만들고  
OpenAI 임베딩 API로 벡터를 만든 뒤  
`product.embedding`에 넣습니다.

## 사용되는 값

처음에는 아래 두 개만 사용합니다.

- `name`
- `description`

예:

```text
상품명: 맥북 프로 14
설명: M3 칩셋, 16GB RAM, 512GB SSD
```

## 현재 흐름

1. 상품 생성 또는 수정
2. `ProductEmbeddingService` 실행
3. `name + description`으로 텍스트 생성
4. 임베딩 생성기 호출
5. 결과 벡터를 `embedding` 필드에 저장

## 관련 클래스

- 상품 저장 서비스: [ProductApplicationService.java](/Users/parkjinwoo/source/study/grepp-BE5/backend5/src/main/java/com/grepp/backend5/product/application/service/ProductApplicationService.java)
- 임베딩 서비스: [ProductEmbeddingService.java](/Users/parkjinwoo/source/study/grepp-BE5/backend5/src/main/java/com/grepp/backend5/product/application/vector/ProductEmbeddingService.java)
- 생성기 인터페이스: [ProductEmbeddingGenerator.java](/Users/parkjinwoo/source/study/grepp-BE5/backend5/src/main/java/com/grepp/backend5/product/application/vector/ProductEmbeddingGenerator.java)
- OpenAI 구현: [OpenAiProductEmbeddingGenerator.java](/Users/parkjinwoo/source/study/grepp-BE5/backend5/src/main/java/com/grepp/backend5/product/infrastructure/vector/OpenAiProductEmbeddingGenerator.java)
- 비활성 구현: [NoOpProductEmbeddingGenerator.java](/Users/parkjinwoo/source/study/grepp-BE5/backend5/src/main/java/com/grepp/backend5/product/infrastructure/vector/NoOpProductEmbeddingGenerator.java)
- 엔티티: [Product.java](/Users/parkjinwoo/source/study/grepp-BE5/backend5/src/main/java/com/grepp/backend5/product/domain/model/Product.java)

## 설정

기본값은 꺼져 있습니다.

```yaml
openai:
  embedding:
    enabled: false
```

실제로 벡터를 만들려면 아래가 필요합니다.

```yaml
openai:
  embedding:
    enabled: true
```

그리고 환경변수도 있어야 합니다.

```bash
OPENAI_API_KEY=...
```

## 켜졌을 때

- `OpenAiProductEmbeddingGenerator`가 선택됩니다
- OpenAI `/v1/embeddings`를 호출합니다
- 결과를 `float[]`로 바꿔서 `product.embedding`에 넣습니다

## 꺼졌을 때

- `NoOpProductEmbeddingGenerator`가 선택됩니다
- 외부 API를 호출하지 않습니다
- 상품은 그냥 저장됩니다
- 벡터는 비어 있을 수 있습니다

## DB 쪽 준비

`product` 테이블에는 아래 컬럼이 있어야 합니다.

- `embedding VECTOR(1536)`

관련 SQL:

- [product-pgvector.sql](/Users/parkjinwoo/source/study/grepp-BE5/backend5/src/main/resources/db/product-pgvector.sql)

## 지금 기억할 것

- 벡터 원본은 `name + description`
- 실제 생성은 OpenAI가 담당
- 켜지지 않으면 상품만 저장되고 벡터는 생성되지 않습니다
