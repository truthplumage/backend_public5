package com.grepp.backend5.search.application;

import com.grepp.backend5.search.infrastructure.dto.ProductDocument;
import com.grepp.backend5.search.presentation.dto.request.IndexConfigRequest;
import com.grepp.backend5.search.presentation.dto.request.ProductIndexRequest;
import com.grepp.backend5.search.presentation.dto.response.IndexStatusResponse;
import com.grepp.backend5.search.presentation.dto.response.IndexUpdateResponse;
import com.grepp.backend5.search.presentation.dto.response.ProductSearchResponse;
import org.springframework.data.domain.Pageable;

public interface SearchUsecase {
    ProductDocument indexProduct(ProductIndexRequest request);
    IndexUpdateResponse applyProductIndexConfig(IndexConfigRequest request);
    IndexStatusResponse getProductIndexStatus();
    ProductSearchResponse searchProducts(String keyword, String category, Pageable pageable);
}
