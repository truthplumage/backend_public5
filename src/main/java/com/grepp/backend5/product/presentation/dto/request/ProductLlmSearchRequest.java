package com.grepp.backend5.product.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "상품 LLM 검색 요청")
public record ProductLlmSearchRequest(
        @Schema(description = "질문 문장", example = "영상 편집용 노트북 추천해줘")
        @NotBlank
        String question,

        @Schema(description = "벡터 검색 개수", example = "5")
        @Min(1)
        @Max(20)
        Integer size
) {
    public int resolvedSize() {
        return size == null ? 5 : size;
    }
}
