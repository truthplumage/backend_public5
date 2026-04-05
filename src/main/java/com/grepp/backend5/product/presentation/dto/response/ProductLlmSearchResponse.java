package com.grepp.backend5.product.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "상품 LLM 검색 응답")
public record ProductLlmSearchResponse(
        @Schema(description = "사용자 질문", example = "영상 편집용 노트북 추천해줘")
        String question,

        @Schema(description = "LLM 답변", example = "맥북 프로 14가 가장 적합합니다. 설명과 성능 정보가 영상 작업 용도와 가장 가깝습니다.")
        String answer,

        @Schema(description = "근거로 사용한 상품 목록")
        List<ProductResponse> products
) {
}
