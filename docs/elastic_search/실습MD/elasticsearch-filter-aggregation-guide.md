## Elasticsearch 상품 필터 집계 가이드

이 문서는 현재 `search` 모듈에 상품 필터 집계 기능을 추가하는 방법을 정리합니다.  
필터 집계는 검색 결과 옆에 `브랜드별 개수`, `카테고리별 개수`, `가격대별 개수`를 같이 보여주는 기능입니다.

예를 들어 `운동화`를 검색했을 때 아래처럼 개수를 함께 반환하는 방식입니다.

```json
{
  "brands": [
    { "key": "NIKE", "count": 12 },
    { "key": "ADIDAS", "count": 8 }
  ],
  "categories": [
    { "key": "shoes", "count": 15 }
  ],
  "priceRanges": [
    { "key": "0-50000", "count": 3 },
    { "key": "50000-100000", "count": 9 },
    { "key": "100000+", "count": 8 }
  ]
}
```

### 구현 목표

- 대상: 상품 검색 결과 기준 필터 집계
- 집계 항목:
  - 브랜드
  - 카테고리
  - 가격대
- 엔드포인트: `GET /api/search/products/filters`

### 구현 방식

이번 문서는 기존 검색 API를 바꾸지 않고, 집계 전용 API를 하나 추가하는 방식으로 작성합니다.

이 방식을 추천하는 이유:

- 기존 `ProductSearchResponse`를 그대로 유지할 수 있음
- 현재 검색 기능을 깨지 않고 확장 가능
- 화면에서도 목록 조회 API와 집계 API를 분리해 붙이기 쉬움

### 클래스 구조

```text
src/main/java/com/grepp/backend5/search
├─ presentation
│  ├─ controller
│  │  └─ ProductSearchController.java
│  └─ dto
│     └─ response
│        ├─ ProductSearchResponse.java
│        ├─ ProductFilterBucketResponse.java
│        └─ ProductFilterAggregationResponse.java
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
| `ProductSearchController` | 필터 집계 API 추가 |
| `SearchUsecase` | 필터 집계 메서드 선언 |
| `SearchService` | Elasticsearch 집계 쿼리 실행 |
| `ProductFilterBucketResponse` | 집계 항목 1건 응답 |
| `ProductFilterAggregationResponse` | 전체 집계 응답 |

### 추가되는 클래스

#### `ProductFilterBucketResponse`

패키지:

`com.grepp.backend5.search.presentation.dto.response`

```java
public record ProductFilterBucketResponse(
        String key,
        long count
) {
}
```

설명:

- `key`: 집계 값
- `count`: 해당 값의 문서 수

예:

- `NIKE`, `12`
- `shoes`, `15`
- `50000-100000`, `9`

#### `ProductFilterAggregationResponse`

패키지:

`com.grepp.backend5.search.presentation.dto.response`

```java
import java.util.List;

public record ProductFilterAggregationResponse(
        List<ProductFilterBucketResponse> brands,
        List<ProductFilterBucketResponse> categories,
        List<ProductFilterBucketResponse> priceRanges
) {
}
```

### 기존 클래스에 추가되는 내용

#### 1. `SearchUsecase`

아래 메서드를 추가합니다.

```java
ProductFilterAggregationResponse aggregateProductFilters(String keyword);
```

### 2. `ProductSearchController`

기존 컨트롤러에 집계 API를 추가합니다.

```java
@Operation(
        summary = "상품 필터 집계",
        description = "검색 키워드를 기준으로 브랜드, 카테고리, 가격대별 개수를 반환합니다."
)
@GetMapping("/products/filters")
public ProductFilterAggregationResponse aggregateProductFilters(
        @Parameter(description = "검색 키워드", example = "운동화")
        @RequestParam(required = false) String keyword
) {
    return searchService.aggregateProductFilters(keyword);
}
```

### 3. `SearchService`

서비스에는 집계 전용 메서드를 추가합니다.

메서드 시그니처:

```java
public ProductFilterAggregationResponse aggregateProductFilters(String keyword)
```

처리 순서:

1. `keyword`가 있으면 `name` 필드 기준 검색 조건 추가
2. `size=0`으로 설정해 문서 목록은 가져오지 않음
3. `brand`, `category`, `price` 기준 집계 추가
4. 집계 결과를 DTO로 변환
5. 응답 반환

### Elasticsearch DSL 형태

필터 집계는 아래와 같은 형태로 생각하면 됩니다.

```json
{
  "size": 0,
  "query": {
    "bool": {
      "must": [
        {
          "match": {
            "name": "운동화"
          }
        }
      ]
    }
  },
  "aggs": {
    "brands": {
      "terms": {
        "field": "brand"
      }
    },
    "categories": {
      "terms": {
        "field": "category"
      }
    },
    "priceRanges": {
      "range": {
        "field": "price",
        "ranges": [
          { "to": 50000, "key": "0-50000" },
          { "from": 50000, "to": 100000, "key": "50000-100000" },
          { "from": 100000, "key": "100000+" }
        ]
      }
    }
  }
}
```

핵심:

- `size: 0`
  - 문서 목록은 필요 없고 집계 결과만 받기 때문
- `terms`
  - `brand`, `category`처럼 정확한 값 개수 계산에 사용
- `range`
  - `price`처럼 구간별 집계에 사용

### Java 구현 예시

현재 프로젝트의 `SearchService` 스타일을 기준으로 하면 아래와 같이 구현할 수 있습니다.

```java
public ProductFilterAggregationResponse aggregateProductFilters(String keyword) {
    NativeQuery query = NativeQuery.builder()
            .withQuery(q -> q.bool(b -> {
                if (keyword != null && !keyword.isBlank()) {
                    b.must(m -> m.match(mm -> mm
                            .field("name")
                            .query(keyword)));
                }
                return b;
            }))
            .withMaxResults(0)
            .withAggregation("brands", Aggregation.of(a -> a
                    .terms(t -> t.field("brand"))))
            .withAggregation("categories", Aggregation.of(a -> a
                    .terms(t -> t.field("category"))))
            .withAggregation("priceRanges", Aggregation.of(a -> a
                    .range(r -> r
                            .field("price")
                            .ranges(range -> range.key("0-50000").to(JsonData.of(50000)))
                            .ranges(range -> range.key("50000-100000").from(JsonData.of(50000)).to(JsonData.of(100000)))
                            .ranges(range -> range.key("100000+").from(JsonData.of(100000))))))
            .build();

    SearchHits<ProductDocument> hits = operations.search(query, ProductDocument.class);

    AggregationsContainer<?> aggregations = hits.getAggregations();

    // brands, categories, priceRanges를 꺼내서
    // ProductFilterBucketResponse 리스트로 변환
    // 최종적으로 ProductFilterAggregationResponse 반환
}
```

구현 포인트:

- `operations.search(...)`는 검색 결과와 집계 결과를 함께 받을 수 있습니다.
- 실제 응답에는 문서 목록이 필요 없으므로 `withMaxResults(0)`로 둡니다.
- 집계 이름은 `brands`, `categories`, `priceRanges`처럼 명확하게 고정합니다.

### API 예시

요청:

```http
GET /api/search/products/filters?keyword=운동화
```

응답:

```json
{
  "brands": [
    { "key": "NIKE", "count": 12 },
    { "key": "ADIDAS", "count": 8 }
  ],
  "categories": [
    { "key": "shoes", "count": 15 }
  ],
  "priceRanges": [
    { "key": "0-50000", "count": 3 },
    { "key": "50000-100000", "count": 9 },
    { "key": "100000+", "count": 8 }
  ]
}
```

### 화면에서 사용하는 방식

화면에서는 보통 아래처럼 사용합니다.

1. 상품 목록 조회
   - `GET /api/search/products`
2. 필터 집계 조회
   - `GET /api/search/products/filters`

이렇게 분리하면:

- 목록 API는 목록에만 집중
- 집계 API는 개수 계산에만 집중

구조가 단순해집니다.

### 개발 순서

1. `ProductFilterBucketResponse` 생성
2. `ProductFilterAggregationResponse` 생성
3. `SearchUsecase`에 `aggregateProductFilters` 선언 추가
4. `SearchService`에 집계 메서드 구현
5. `ProductSearchController`에 `/products/filters` 추가
6. Swagger 설명 추가
7. Elasticsearch에 샘플 데이터 입력 후 확인

### 테스트 포인트

- `keyword` 없이 호출해도 전체 집계가 나오는지
- `keyword`를 넣으면 해당 검색 결과 기준으로 집계되는지
- 브랜드 집계가 정상인지
- 카테고리 집계가 정상인지
- 가격대 집계가 정상인지
- 결과가 없으면 각 리스트가 빈 배열인지

### 기억할 것

- 필터 집계는 검색 결과 옆에 붙는 "조건별 개수"입니다.
- `terms`는 문자 집계에, `range`는 숫자 구간 집계에 적합합니다.
- 현재 구조에서는 집계 전용 API를 따로 두는 것이 가장 단순합니다.
