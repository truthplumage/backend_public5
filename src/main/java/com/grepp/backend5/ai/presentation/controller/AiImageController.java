package com.grepp.backend5.ai.presentation.controller;

import com.grepp.backend5.ai.application.service.AiImageService;
import com.grepp.backend5.ai.presentation.dto.request.ImageGenerationRequest;
import com.grepp.backend5.ai.presentation.dto.response.ImageAnalysisResponse;
import com.grepp.backend5.ai.presentation.dto.response.ImageGenerationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/ai/images")
@RequiredArgsConstructor
@Tag(name = "AI Image", description = "이미지 분석 및 생성 API")
public class AiImageController {

    private final AiImageService aiImageService;

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "이미지 보내기", description = "이미지를 업로드해서 내용 설명을 받습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "분석 성공",
                    content = @Content(schema = @Schema(implementation = ImageAnalysisResponse.class)))
    })
    public ImageAnalysisResponse analyzeImage(
            @RequestPart("image") MultipartFile image,
            @RequestParam(name = "prompt", required = false) String prompt
    ) {
        return aiImageService.analyzeImage(image, prompt);
    }

    @PostMapping("/generate")
    @Operation(summary = "이미지 받기", description = "프롬프트로 이미지를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공",
                    content = @Content(schema = @Schema(implementation = ImageGenerationResponse.class)))
    })
    public ImageGenerationResponse generateImage(@Valid @RequestBody ImageGenerationRequest request) {
        return aiImageService.generateImage(request);
    }
}
