## Elasticsearch 인기검색어 가이드

이 문서는 현재 `search` 모듈에 인기검색어 기능을 추가하는 방법을 정리합니다.  
인기검색어는 "사용자가 어떤 검색어를 많이 입력했는가"를 보여주는 기능입니다.

예:

- 운동화
- 나이키
- 셔츠
- 샌들

### 구현 목표

- 상품 검색 시 검색어를 기록
- 기록된 검색어를 집계
- 많이 검색된 순서대로 조회

### 구현 방식

이번 문서는 아래 방식으로 정리합니다.

1. 상품 검색 API가 호출되면 검색어를 저장
2. 검색어는 별도 Elasticsearch 인덱스에 기록
3. 인기검색어 API가 집계 결과를 반환

이 방식을 쓰는 이유:

- 상품 검색 데이터와 검색어 기록을 분리할 수 있음
- 인기검색어 집계를 만들기 쉬움
- 기간 조건(`최근 7일`, `최근 30일`)을 넣기 쉬움

### 1차 구현 기준

- 상품 검색 인덱스: `shop-products`
- 검색어 기록 인덱스: `search-keywords`
- 인기검색어 조회 API: `GET /api/search/keywords/popular`

### 클래스 구조

```text
src/main/java/com/grepp/backend5/search
├─ presentation
│  ├─ controller
│  │  ├─ ProductSearchController.java
│  │  └─ SearchKeywordController.java
│  └─ dto
│     └─ response
│        ├─ ProductSearchResponse.java
│        ├─ PopularKeywordItemResponse.java
│        └─ PopularKeywordResponse.java
├─ application
│  ├─ SearchService.java
│  ├─ SearchKeywordUsecase.java
│  └─ SearchKeywordService.java
└─ infrastructure
   ├─ ProductSearchRepository.java
   ├─ SearchKeywordRepository.java
   └─ dto
      ├─ ProductDocument.java
      └─ SearchKeywordDocument.java
```

### 클래스별 역할

| 클래스 | 역할 |
| --- | --- |
| `ProductSearchController` | 상품 검색 API |
| `SearchKeywordController` | 인기검색어 조회 API |
| `SearchService` | 상품 검색 수행, 검색어 기록 호출 |
| `SearchKeywordUsecase` | 검색어 기록/집계 메서드 선언 |
| `SearchKeywordService` | 검색어 저장과 집계 처리 |
| `SearchKeywordDocument` | 검색어 기록용 Elasticsearch 문서 |
| `SearchKeywordRepository` | 검색어 기록 저장소 |
| `PopularKeywordItemResponse` | 인기검색어 항목 1건 응답 |
| `PopularKeywordResponse` | 인기검색어 목록 응답 |

### 추가되는 클래스

#### `SearchKeywordDocument`

패키지:

`com.grepp.backend5.search.infrastructure.dto`

```java
@Document(indexName = "search-keywords")
public class SearchKeywordDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String keyword;

    @Field(type = FieldType.Keyword)
    private String normalizedKeyword;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant searchedAt;
}
```

필드 설명:

- `keyword`
  - 사용자가 실제 입력한 값
- `normalizedKeyword`
  - 집계용 값
  - `trim()`, `lowercase()` 처리한 값 권장
- `searchedAt`
  - 검색 시각

#### `SearchKeywordRepository`

패키지:

`com.grepp.backend5.search.infrastructure`

```java
public interface SearchKeywordRepository
        extends ElasticsearchRepository<SearchKeywordDocument, String> {
}
```

#### `PopularKeywordItemResponse`

패키지:

`com.grepp.backend5.search.presentation.dto.response`

```java
public record PopularKeywordItemResponse(
        String keyword,
        long count
) {
}
```

#### `PopularKeywordResponse`

패키지:

`com.grepp.backend5.search.presentation.dto.response`

```java
import java.util.List;

public record PopularKeywordResponse(
        List<PopularKeywordItemResponse> items
) {
}
```

### 기존 클래스에 추가되는 내용

#### 1. `SearchKeywordUsecase`

```java
public interface SearchKeywordUsecase {
    void recordKeyword(String keyword);
    PopularKeywordResponse getPopularKeywords(int days, int size);
}
```

#### 2. `SearchKeywordService`

역할은 두 가지입니다.

1. 검색어 저장
2. 인기검색어 집계

메서드 예시:

```java
public void recordKeyword(String keyword)
public PopularKeywordResponse getPopularKeywords(int days, int size)
```

##### `recordKeyword`

처리 순서:

1. `keyword`가 비어 있으면 저장하지 않음
2. `trim()` 처리
3. 소문자 기준 `normalizedKeyword` 생성
4. 현재 시각과 함께 문서 저장

예시 코드:

```java
public void recordKeyword(String keyword) {
    if (keyword == null || keyword.isBlank()) {
        return;
    }

    String trimmed = keyword.trim();

    SearchKeywordDocument document = new SearchKeywordDocument(
            null,
            trimmed,
            trimmed.toLowerCase(),
            Instant.now()
    );

    repository.save(document);
}
```

##### `getPopularKeywords`

처리 순서:

1. 기준 날짜 계산
2. `searchedAt` 범위 필터 적용
3. `normalizedKeyword` 기준 `terms aggregation`
4. 상위 `size`개 추출
5. 응답 DTO로 변환

### 인기검색어 집계 DSL

아래 형태로 생각하면 됩니다.

```json
{
  "size": 0,
  "query": {
    "range": {
      "searchedAt": {
        "gte": "now-7d/d"
      }
    }
  },
  "aggs": {
    "popularKeywords": {
      "terms": {
        "field": "normalizedKeyword",
        "size": 10,
        "order": {
          "_count": "desc"
        }
      }
    }
  }
}
```

핵심:

- `size: 0`
  - 문서 목록은 필요 없고 집계만 필요하기 때문
- `range`
  - 최근 7일, 최근 30일 같은 기간 조건에 사용
- `terms`
  - 같은 검색어를 묶어서 개수 계산

### `SearchService`에서 해야 할 변경

기존 상품 검색 메서드 안에서 검색어 기록을 같이 호출합니다.

예:

```java
public ProductSearchResponse searchProducts(String keyword, String category, Pageable pageable) {
    if (keyword != null && !keyword.isBlank()) {
        searchKeywordUsecase.recordKeyword(keyword);
    }

    // 기존 상품 검색 로직 수행
}
```

즉 상품 검색은 그대로 두고, 검색어 기록만 추가하는 방식입니다.

### `SearchKeywordController`

인기검색어 조회 전용 컨트롤러를 따로 둡니다.

패키지:

`com.grepp.backend5.search.presentation.controller`

```java
@RestController
@RequestMapping("/api/search/keywords")
@RequiredArgsConstructor
public class SearchKeywordController {

    private final SearchKeywordUsecase searchKeywordUsecase;

    @GetMapping("/popular")
    public PopularKeywordResponse getPopularKeywords(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "10") int size
    ) {
        return searchKeywordUsecase.getPopularKeywords(days, size);
    }
}
```

Swagger 예시:

```java
@Operation(
        summary = "인기검색어 조회",
        description = "최근 기간 기준으로 많이 검색된 키워드를 반환합니다."
)
@GetMapping("/popular")
public PopularKeywordResponse getPopularKeywords(
        @Parameter(description = "조회 기간(일)", example = "7")
        @RequestParam(defaultValue = "7") int days,
        @Parameter(description = "최대 반환 개수", example = "10")
        @RequestParam(defaultValue = "10") int size
) {
    return searchKeywordUsecase.getPopularKeywords(days, size);
}
```

### API 예시

요청:

```http
GET /api/search/keywords/popular?days=7&size=5
```

응답:

```json
{
  "items": [
    { "keyword": "운동화", "count": 120 },
    { "keyword": "나이키", "count": 98 },
    { "keyword": "샌들", "count": 65 }
  ]
}
```

### 개발 순서

1. `SearchKeywordDocument` 생성
2. `SearchKeywordRepository` 생성
3. `PopularKeywordItemResponse` 생성
4. `PopularKeywordResponse` 생성
5. `SearchKeywordUsecase` 생성
6. `SearchKeywordService` 구현
7. `SearchService`에서 검색어 기록 호출 추가
8. `SearchKeywordController` 추가
9. Elasticsearch에 검색어 기록이 쌓이는지 확인
10. 인기검색어 API 확인

### 테스트 포인트

- `keyword`가 비어 있으면 저장되지 않는지
- 검색 시 검색어 기록이 생성되는지
- 최근 7일 기준 집계가 정상인지
- `size`만큼만 반환되는지
- 많이 검색된 순서대로 내려오는지
- 기록이 없으면 빈 배열을 반환하는지

### 이후 확장 방향

1차 구현이 동작하면 아래를 고려할 수 있습니다.

- 사용자별 검색어 제외 처리
- 봇/중복 요청 필터링
- Redis로 실시간 인기검색어 캐시
- Kafka 이벤트 기반 비동기 기록
- 시간대별 인기검색어 분리

### 기억할 것

- 인기검색어는 상품 데이터가 아니라 검색 기록 데이터입니다.
- 그래서 `shop-products`와 별도 인덱스로 분리하는 것이 좋습니다.
- 현재 구조에서는 `검색 수행`과 `검색어 집계`를 분리하는 쪽이 가장 관리하기 쉽습니다.
