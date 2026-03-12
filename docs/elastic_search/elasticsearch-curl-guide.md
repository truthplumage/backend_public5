# Elasticsearch curl 실행 가이드

현재 프로젝트 기준으로 Elasticsearch를 `curl`로 직접 호출하는 예시입니다.

### 기준 정보

- 주소: `http://localhost:9200`
- 버전: `9.2.5`
- 인덱스: `shop-products`

### 1. 연결 확인

```bash
curl http://localhost:9200
```

### 2. 인덱스 생성

```bash
curl -X PUT "http://localhost:9200/shop-products" \
  -H "Content-Type: application/json" \
  -d '{
    "mappings": {
      "properties": {
        "name":      { "type": "text" },
        "brand":     { "type": "keyword" },
        "category":  { "type": "keyword" },
        "price":     { "type": "integer" },
        "updatedAt": { "type": "date" }
      }
    }
  }'
```

### 3. 문서 등록

```bash
curl -X PUT "http://localhost:9200/shop-products/_doc/sku-1001?refresh=true" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "나이키 운동화",
    "brand": "NIKE",
    "category": "shoes",
    "price": 129000,
    "updatedAt": "2026-03-13T10:00:00Z"
  }'
```

### 4. 전체 조회

```bash
curl "http://localhost:9200/shop-products/_search?pretty"
```

### 5. 이름 검색

```bash
curl -X POST "http://localhost:9200/shop-products/_search?pretty" \
  -H "Content-Type: application/json" \
  -d '{
    "query": {
      "match": {
        "name": "운동화"
      }
    }
  }'
```

### 6. 카테고리 검색

```bash
curl -X POST "http://localhost:9200/shop-products/_search?pretty" \
  -H "Content-Type: application/json" \
  -d '{
    "query": {
      "term": {
        "category": "shoes"
      }
    }
  }'
```

### 7. 상태 확인

```bash
curl "http://localhost:9200/_cat/indices?v"
```

```bash
curl "http://localhost:9200/shop-products/_mapping?pretty"
```

### 8. 삭제

```bash
curl -X DELETE "http://localhost:9200/shop-products"
```

### 빠른 순서

1. 연결 확인
2. 인덱스 생성
3. 문서 등록
4. 전체 조회
5. 검색 실행
