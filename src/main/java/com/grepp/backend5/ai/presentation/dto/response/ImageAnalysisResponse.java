package com.grepp.backend5.ai.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이미지 분석 응답")
public record ImageAnalysisResponse(
        @Schema(description = "사용한 질문", example = "이 이미지를 설명해줘")
        String prompt,

        @Schema(description = "LLM 분석 결과", example = "노트북이 책상 위에 놓여 있고 화면에는 편집 프로그램이 보입니다.")
        String answer
) {
}
