package com.grepp.backend5.search.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "상품 색인 요청 DTO",
        example = "{\n" +
                "  \"name\": \"남자 셔츠\",\n" +
                "  \"brand\": \"SHOP\",\n" +
                "  \"category\": \"shirts\",\n" +
                "  \"price\": 59000\n" +
                "}"
)
// 상품 색인 요청 DTO (id/updatedAt은 서버에서 자동 생성)
public record ProductIndexRequest(
    String name,
    String brand,
    String category,
    Integer price
) {
}
