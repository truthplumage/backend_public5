package com.grepp.backend5.search.application;

import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import com.grepp.backend5.search.infrastructure.ProductSearchRepository;
import com.grepp.backend5.search.infrastructure.dto.ProductDocument;
import com.grepp.backend5.search.presentation.dto.request.IndexConfigRequest;
import com.grepp.backend5.search.presentation.dto.request.ProductIndexRequest;
import com.grepp.backend5.search.presentation.dto.response.IndexStatusResponse;
import com.grepp.backend5.search.presentation.dto.response.IndexUpdateResponse;
import com.grepp.backend5.search.presentation.dto.response.ProductSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 키워드/카테고리 기준으로 상품 검색 쿼리를 빌드하고 실행
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "search.elasticsearch", name = "enabled", havingValue = "true")
public class SearchService implements SearchUsecase{

    private final ElasticsearchOperations operations;
    private final ProductSearchRepository repository;

    // 상품 문서를 ES에 저장(id는 ES 자동 생성, updatedAt은 현재 시각)
    public ProductDocument indexProduct(ProductIndexRequest request) {
        Instant updatedAt = Instant.now();
        ProductDocument doc = new ProductDocument(
            null, // id를 비우면 ES가 자동 생성
            request.name(),
            request.brand(),
            request.category(),
            request.price(),
            updatedAt
        );
        return repository.save(doc);
    }

    // 인덱스가 없으면 설정/매핑과 함께 생성, 있으면 매핑만 갱신
    public IndexUpdateResponse applyProductIndexConfig(IndexConfigRequest request) {
        IndexOperations ops = operations.indexOps(ProductDocument.class);
        boolean created = false;
        boolean settingsUpdated = false;
        boolean mappingUpdated = false;

        if (!ops.exists()) {
            Document settings = Document.create();
            if (request.numberOfShards() != null) {
                settings.put("index.number_of_shards", request.numberOfShards());
            }
            if (request.numberOfReplicas() != null) {
                settings.put("index.number_of_replicas", request.numberOfReplicas());
            }
            created = ops.create(settings);
            mappingUpdated = ops.putMapping(ops.createMapping(ProductDocument.class));
        } else {
            // 기존 인덱스는 샤드 수 변경이 불가. 레플리카/매핑 변경은 별도 관리 API에서 수행하거나 추후 확장.
            mappingUpdated = ops.putMapping(ops.createMapping(ProductDocument.class));
        }

        return new IndexUpdateResponse(created, settingsUpdated, mappingUpdated);
    }

    // 인덱스 존재 여부, 설정, 매핑 정보를 조회
    public IndexStatusResponse getProductIndexStatus() {
        IndexOperations ops = operations.indexOps(ProductDocument.class);
        boolean exists = ops.exists();
        Map<String, Object> settings = exists ? new HashMap<>(ops.getSettings()) : Map.of();
        Map<String, Object> mapping = exists ? new HashMap<>(ops.getMapping()) : Map.of();
        return new IndexStatusResponse(exists, settings, mapping);
    }

    public ProductSearchResponse searchProducts(String keyword, String category, Pageable pageable) {
        NativeQuery query = NativeQuery.builder()
            .withQuery(q -> q.bool(b -> {
                if (keyword != null && !keyword.isBlank()) {
                    b.must(//조건이 유추가 아닌 정확하게 맞아야 함을 의미
                            m -> m.match(
                                    mm -> mm
                        .field("name")
                        .query(keyword)
                        .operator(Operator.And) // "남자" AND "신발" 식으로 토큰 모두 매칭
                    ));
                }
                if (category != null && !category.isBlank()) {
                    b.filter(f -> f.term(// term으로 정확히 같은 카테고리 값만 매칭
                            t -> t.field("category").value(category)));
                }
                return b;
            }))
            .withPageable(pageable)
            .build();

        SearchHits<ProductDocument> hits = operations.search(query, ProductDocument.class);
        List<ProductDocument> items = hits.getSearchHits().stream()
            .map(SearchHit::getContent)
            .toList();

        return new ProductSearchResponse(hits.getTotalHits(), items);
    }
}
