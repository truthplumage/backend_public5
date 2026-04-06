package com.grepp.backend5.ai.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "이미지 생성 요청")
public record ImageGenerationRequest(
        @Schema(description = "이미지 생성 프롬프트", example = "노을지는 바다를 바라보는 흰 고양이")
        @NotBlank
        String prompt,

        @Schema(description = "이미지 크기", example = "1024x1024")
        String size
) {
    public String resolvedSize() {
        return size == null || size.isBlank() ? "1024x1024" : size;
    }
}
