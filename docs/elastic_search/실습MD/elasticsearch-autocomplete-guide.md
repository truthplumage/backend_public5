## Elasticsearch 상품 자동완성 가이드

이 문서는 현재 `search` 모듈 구조를 유지한 상태에서 상품 자동완성 기능을 추가하는 방법을 정리합니다.  
목표는 "검색창에 입력하는 동안 상품명 후보를 빠르게 보여주는 기능"입니다.

### 구현 목표

- 자동완성 대상: 상품명
- 엔드포인트: `GET /api/search/products/suggest`
- 입력값: `keyword`, `size`
- 출력값: 자동완성 후보 목록

현재 프로젝트에서는 검색 기능이 이미 `search` 모듈에 있으므로, 자동완성도 같은 모듈 안에서 확장하는 것이 가장 단순합니다.

### 1차 구현 기준

1차 구현은 새 인덱스를 만들지 않고 기존 `shop-products` 인덱스를 그대로 사용합니다.  
자동완성 쿼리는 `name` 필드 기준으로 처리합니다.

이 방식의 장점:

- 현재 구조를 거의 그대로 사용 가능
- 새 Repository가 필요 없음
- 코드 변경 범위가 작음

### 클래스 구조

```text
src/main/java/com/grepp/backend5/search
├─ presentation
│  ├─ controller
│  │  └─ ProductSearchController.java
│  └─ dto
│     └─ response
│        ├─ ProductSearchResponse.java
│        ├─ ProductSuggestItemResponse.java
│        └─ ProductSuggestResponse.java
├─ application
│  ├─ SearchUsecase.java
│  └─ SearchService.java
└─ infrastructure
   ├─ ProductSearchRepository.java
   └─ dto
      └─ ProductDocument.java
```

### 클래스별 역할

| 클래스 | 역할 |
| --- | --- |
| `ProductSearchController` | 자동완성 API 추가 |
| `SearchUsecase` | 자동완성 메서드 선언 |
| `SearchService` | Elasticsearch 자동완성 쿼리 실행 |
| `ProductSuggestItemResponse` | 자동완성 항목 1건 응답 |
| `ProductSuggestResponse` | 자동완성 목록 응답 |
| `ProductDocument` | 자동완성 대상이 되는 ES 문서 |

### 추가되는 클래스

#### `ProductSuggestItemResponse`

패키지:

`com.grepp.backend5.search.presentation.dto.response`

예상 형태:

```java
public record ProductSuggestItemResponse(
        String id,
        String name,
        String brand
) {
}
```

#### `ProductSuggestResponse`

패키지:

`com.grepp.backend5.search.presentation.dto.response`

예상 형태:

```java
import java.util.List;

public record ProductSuggestResponse(
        List<ProductSuggestItemResponse> items
) {
}
```

### 기존 클래스에 추가되는 내용

#### 1. `SearchUsecase`

아래 메서드를 추가합니다.

```java
ProductSuggestResponse suggestProducts(String keyword, int size);
```

#### 2. `SearchService`

자동완성 메서드를 추가합니다.

메서드 시그니처:

```java
public ProductSuggestResponse suggestProducts(String keyword, int size)
```

처리 순서:

1. `keyword`가 비어 있으면 빈 리스트 반환
2. Elasticsearch 쿼리 생성
3. 최대 `size`건 조회
4. `ProductDocument`를 자동완성 응답 DTO로 변환
5. 결과 반환

예시 코드:

```java
public ProductSuggestResponse suggestProducts(String keyword, int size) {
    if (keyword == null || keyword.isBlank()) {
        return new ProductSuggestResponse(List.of());
    }

    NativeQuery query = NativeQuery.builder()
            .withQuery(q -> q.matchPhrasePrefix(m -> m
                    .field("name")
                    .query(keyword)))
            .withPageable(PageRequest.of(0, size))
            .build();

    SearchHits<ProductDocument> hits = operations.search(query, ProductDocument.class);

    List<ProductSuggestItemResponse> items = hits.getSearchHits().stream()
            .map(SearchHit::getContent)
            .map(doc -> new ProductSuggestItemResponse(
                    doc.getId(),
                    doc.getName(),
                    doc.getBrand()
            ))
            .toList();

    return new ProductSuggestResponse(items);
}
```

설명:

- `matchPhrasePrefix`는 입력 중인 키워드 기준으로 앞부분이 맞는 결과를 찾을 때 사용하기 쉽습니다.
- 1차 구현에서는 별도 `suggest` 필드 없이 `name` 필드만 사용합니다.

#### 3. `ProductSearchController`

기존 컨트롤러에 자동완성 엔드포인트를 추가합니다.

메서드 예시:

```java
@GetMapping("/products/suggest")
public ProductSuggestResponse suggestProducts(
        @RequestParam String keyword,
        @RequestParam(defaultValue = "10") int size
) {
    return searchService.suggestProducts(keyword, size);
}
```

Swagger 예시까지 넣으면 아래와 비슷한 형태가 됩니다.

```java
@Operation(
        summary = "상품 자동완성",
        description = "입력한 키워드 기준으로 상품명 자동완성 목록을 조회합니다."
)
@GetMapping("/products/suggest")
public ProductSuggestResponse suggestProducts(
        @Parameter(description = "자동완성 키워드", example = "나이")
        @RequestParam String keyword,
        @Parameter(description = "최대 반환 개수", example = "10")
        @RequestParam(defaultValue = "10") int size
) {
    return searchService.suggestProducts(keyword, size);
}
```

### API 형태

요청:

```http
GET /api/search/products/suggest?keyword=나이&size=5
```

응답:

```json
{
  "items": [
    {
      "id": "sku-1001",
      "name": "나이키 운동화",
      "brand": "NIKE"
    },
    {
      "id": "sku-1002",
      "name": "나이키 러닝화",
      "brand": "NIKE"
    }
  ]
}
```

### 개발 순서

1. `ProductSuggestItemResponse` 생성
2. `ProductSuggestResponse` 생성
3. `SearchUsecase`에 `suggestProducts` 선언 추가
4. `SearchService`에 자동완성 메서드 구현
5. `ProductSearchController`에 `/products/suggest` 추가
6. Swagger 설명 추가
7. `curl` 또는 Swagger UI로 동작 확인

### 테스트 포인트

- `keyword`가 비어 있으면 빈 배열 반환
- `size`만큼만 결과 반환
- `name` 기준으로 자동완성 결과가 나오는지 확인
- 결과가 없으면 빈 배열 반환

### 이후 확장 방향

1차 구현이 동작하면 그 다음에 아래를 고려할 수 있습니다.

- `name` 외에 `brand` 포함 자동완성
- 별도 `suggest` 필드 추가
- 한국어 품질 개선을 위한 분석기 적용
- 인기순 자동완성 정렬

하지만 처음부터 복잡하게 가지 말고, 먼저 현재 구조에서 `name` 기준 자동완성부터 붙이는 것이 가장 안전합니다.
