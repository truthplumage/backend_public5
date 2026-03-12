## Elasticsearch 운영 메모

운영에서 자주 보는 것은 많지 않습니다.  
현재 프로젝트에서는 아래 4가지만 자주 확인하면 됩니다.

### 기준 정보

- Elasticsearch 주소: `http://localhost:9200`
- 버전: `9.2.5`
- 인덱스 예시: `shop-products`

### 1. 클러스터 상태 확인

```bash
curl "http://localhost:9200/_cluster/health?pretty"
```

보는 값:
- `status`
- `number_of_nodes`

### 2. 인덱스 목록 확인

```bash
curl "http://localhost:9200/_cat/indices?v"
```

이 명령으로 인덱스 이름, 문서 수, 저장 크기를 볼 수 있습니다.

### 3. 샤드 상태 확인

```bash
curl "http://localhost:9200/_cat/shards/shop-products?v"
```

이 명령으로 `shop-products` 인덱스의 샤드 배치를 볼 수 있습니다.

### 4. 인덱스 설정 확인

```bash
curl "http://localhost:9200/shop-products/_settings?pretty"
```

여기서 주로 보는 값:
- `number_of_shards`
- `number_of_replicas`

### 샤드를 왜 보는가

- 데이터가 많아지면 검색 부하를 나눌 수 있습니다.
- 너무 많으면 오히려 관리 비용이 커집니다.

로컬에서는 보통 이렇게 시작하면 충분합니다.

- `number_of_shards`: 1~3
- `number_of_replicas`: 0

### 기억할 것

- 로컬은 단일 노드이므로 복잡한 분산 운영까지 바로 볼 필요는 없습니다.
- 먼저 클러스터 상태, 인덱스 목록, 샤드 수만 확인하면 됩니다.
- 인덱스가 정상이고 검색이 되면 운영 점검의 대부분은 끝입니다.
