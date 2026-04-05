package com.grepp.backend5.product.infrastructure.vector;

import com.grepp.backend5.product.application.vector.ProductEmbeddingGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
@ConditionalOnProperty(prefix = "openai.embedding", name = "enabled", havingValue = "true")
public class OpenAiProductEmbeddingGenerator implements ProductEmbeddingGenerator {

    private static final String EMBEDDING_URL = "https://api.openai.com/v1/embeddings";

    private final RestClient restClient;
    @Value("${openai.embedding.api-key:}")
    private String apiKey;
    @Value("${openai.embedding.model:text-embedding-3-small}")
    private String model;

    @Override
    public Optional<float[]> generate(String text) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OpenAI embedding api key is not configured");
        }

        EmbeddingResponse response = restClient.post()
                .uri(EMBEDDING_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .body(new EmbeddingRequest(model, text))
                .retrieve()
                .body(EmbeddingResponse.class);

        if (response == null || response.data() == null || response.data().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(toFloatArray(response.data().get(0).embedding()));
    }

    private float[] toFloatArray(List<Double> values) {
        float[] embedding = new float[values.size()];
        for (int index = 0; index < values.size(); index++) {
            embedding[index] = values.get(index).floatValue();
        }
        return embedding;
    }

    private record EmbeddingRequest(
            String model,
            String input
    ) {
    }

    private record EmbeddingResponse(
            List<EmbeddingData> data
    ) {
    }

    private record EmbeddingData(
            List<Double> embedding
    ) {
    }
}
