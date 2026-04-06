package com.grepp.backend5.ai.application.service;

import com.grepp.backend5.ai.infrastructure.client.OpenAiImageClient;
import com.grepp.backend5.ai.presentation.dto.request.ImageGenerationRequest;
import com.grepp.backend5.ai.presentation.dto.response.ImageAnalysisResponse;
import com.grepp.backend5.ai.presentation.dto.response.ImageGenerationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AiImageService {

    private final OpenAiImageClient openAiImageClient;

    public ImageAnalysisResponse analyzeImage(MultipartFile image, String prompt) {
        String resolvedPrompt = prompt == null || prompt.isBlank() ? "이 이미지를 설명해줘" : prompt;
        return openAiImageClient.analyze(image, resolvedPrompt);
    }

    public ImageGenerationResponse generateImage(ImageGenerationRequest request) {
        return openAiImageClient.generate(request);
    }
}
