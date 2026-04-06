package com.grepp.backend5.ai.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이미지 생성 응답")
public record ImageGenerationResponse(
        @Schema(description = "사용한 프롬프트", example = "노을지는 바다를 바라보는 흰 고양이")
        String prompt,

        @Schema(description = "이미지 형식", example = "png")
        String format,

        @Schema(description = "base64 이미지 데이터", example = "iVBORw0KGgoAAAANSUhEUgAA...")
        String imageBase64
) {
}
