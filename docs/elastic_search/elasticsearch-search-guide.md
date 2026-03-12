## Elasticsearch 검색 구성 가이드

현재 프로젝트는 상품 검색용으로 Elasticsearch를 사용합니다.  
핵심만 보면 아래 순서입니다.

1. Elasticsearch 실행
2. `shop-products` 인덱스 준비
3. 애플리케이션에서 검색 요청 처리
4. 결과 반환

### 현재 프로젝트 기준 정보

- Elasticsearch 주소: `http://localhost:9200`
- Elasticsearch 버전: `9.2.5`
- 인덱스 이름: `shop-products`
- 검색 API 경로: `/api/search/products`
- 인덱스 확인 API 경로: `/api/search/products/index`

### 동작 흐름

```mermaid
flowchart LR
    A["검색 요청"] --> B["ProductSearchController"]
    B --> C["SearchService"]
    C --> D["Elasticsearch"]
    D --> C
    C --> B
```

### 1. Elasticsearch 실행

```bash
docker run -d --name shop-es -p 9200:9200 \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  docker.elastic.co/elasticsearch/elasticsearch:9.2.5
```

확인:

```bash
curl http://localhost:9200
```

### 2. 의존성

`build.gradle`

```groovy
implementation 'org.springframework.boot:spring-boot-starter-data-elasticsearch'
```

### 3. 설정

`src/main/resources/application.yaml`

```yaml
spring:
  elasticsearch:
    uris: http://localhost:9200
```

### 4. 인덱스 준비

```bash
curl -X PUT "http://localhost:9200/shop-products" \
  -H "Content-Type: application/json" \
  -d '{
    "mappings": {
      "properties": {
        "id":        { "type": "keyword" },
        "name":      { "type": "text" },
        "brand":     { "type": "keyword" },
        "category":  { "type": "keyword" },
        "price":     { "type": "integer" },
        "updatedAt": { "type": "date" }
      }
    }
  }'
```

### 5. 코드에서 연결되는 위치

- 컨트롤러: `com.grepp.backend5.search.presentation.controller.ProductSearchController`
- 서비스: `com.grepp.backend5.search.application.SearchService`
- 문서 모델: `com.grepp.backend5.search.infrastructure.dto.ProductDocument`

즉, 요청은 컨트롤러로 들어오고 서비스가 Elasticsearch를 조회한 뒤 결과를 반환합니다.

### 6. 기억할 것

- 검색 데이터는 `shop-products` 인덱스에 저장됩니다.
- `ProductDocument`가 Elasticsearch 문서 구조입니다.
- 검색 API는 Elasticsearch를 직접 조회합니다.
- 로컬에서는 단일 노드로 시작하면 충분합니다.
