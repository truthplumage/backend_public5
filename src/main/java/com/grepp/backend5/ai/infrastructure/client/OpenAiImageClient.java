package com.grepp.backend5.ai.infrastructure.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.grepp.backend5.ai.presentation.dto.request.ImageGenerationRequest;
import com.grepp.backend5.ai.presentation.dto.response.ImageAnalysisResponse;
import com.grepp.backend5.ai.presentation.dto.response.ImageGenerationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Component
public class OpenAiImageClient {

    private static final String RESPONSES_URL = "https://api.openai.com/v1/responses";
    private static final String IMAGE_GENERATION_URL = "https://api.openai.com/v1/images/generations";

    private final WebClient webClient;
    private final String apiKey;
    private final String imageAnalysisModel;
    private final String imageModel;

    public OpenAiImageClient(WebClient webClient,
                             @Value("${openai.image.api-key:${OPENAI_API_KEY:}}") String apiKey,
                             @Value("${openai.image-analysis.model:gpt-4.1-mini}") String imageAnalysisModel,
                             @Value("${openai.image.model:gpt-image-1}") String imageModel) {
        this.webClient = webClient;
        this.apiKey = apiKey;
        this.imageAnalysisModel = imageAnalysisModel;
        this.imageModel = imageModel;
    }

    public ImageAnalysisResponse analyze(MultipartFile image, String prompt) {
        validateApiKey();
        String contentType = image.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : image.getContentType();
        String dataUrl = "data:%s;base64,%s".formatted(contentType, encode(image));

        ResponseTextResult response = webClient.post()
                .uri(RESPONSES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .bodyValue(new VisionRequest(
                        imageAnalysisModel,
                        List.of(new InputMessage(
                                "user",
                                List.of(
                                        new InputText("input_text", prompt),
                                        new InputImage("input_image", dataUrl)
                                )
                        ))
                ))
                .retrieve()
                .bodyToMono(ResponseTextResult.class)
                .block();

        String answer = response == null || response.outputText() == null || response.outputText().isBlank()
                ? "응답을 받지 못했습니다."
                : response.outputText().trim();
        return new ImageAnalysisResponse(prompt, answer);
    }

    public ImageGenerationResponse generate(ImageGenerationRequest request) {
        validateApiKey();

        ImageGenerationResult response = webClient.post()
                .uri(IMAGE_GENERATION_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .bodyValue(new ImageGenerationPayload(
                        imageModel,
                        request.prompt(),
                        request.resolvedSize()
                ))
                .retrieve()
                .bodyToMono(ImageGenerationResult.class)
                .block();

        String imageBase64 = response == null || response.data() == null || response.data().isEmpty()
                ? ""
                : response.data().get(0).b64Json();
        return new ImageGenerationResponse(request.prompt(), "png", imageBase64);
    }

    private void validateApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OpenAI api key is not configured");
        }
    }

    private String encode(MultipartFile image) {
        try {
            return Base64.getEncoder().encodeToString(image.getBytes());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read image bytes", exception);
        }
    }

    private record VisionRequest(
            String model,
            List<InputMessage> input
    ) {
    }

    private record InputMessage(
            String role,
            List<Object> content
    ) {
    }

    private record InputText(
            String type,
            String text
    ) {
    }

    private record InputImage(
            String type,
            @JsonProperty("image_url")
            String imageUrl
    ) {
    }

    private record ResponseTextResult(
            @JsonProperty("output_text")
            String outputText
    ) {
    }

    private record ImageGenerationPayload(
            String model,
            String prompt,
            String size
    ) {
    }

    private record ImageGenerationResult(
            List<ImageData> data
    ) {
    }

    private record ImageData(
            @JsonProperty("b64_json")
            String b64Json
    ) {
    }
}
