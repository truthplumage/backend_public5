package com.grepp.backend5.product.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 임베딩 재생성 결과")
public record ProductEmbeddingRefreshResponse(
        @Schema(description = "전체 상품 수", example = "10")
        int totalCount,

        @Schema(description = "실제로 임베딩이 갱신된 상품 수", example = "8")
        int updatedCount
) {
}
