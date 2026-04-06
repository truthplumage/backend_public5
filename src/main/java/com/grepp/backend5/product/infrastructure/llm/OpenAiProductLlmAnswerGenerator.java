package com.grepp.backend5.product.infrastructure.llm;

import com.grepp.backend5.product.application.llm.ProductLlmAnswerGenerator;
import com.grepp.backend5.product.domain.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Component
@ConditionalOnProperty(prefix = "openai.chat", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class OpenAiProductLlmAnswerGenerator implements ProductLlmAnswerGenerator {

    private static final String CHAT_COMPLETIONS_URL =
            "https://api.openai.com/v1/chat/completions";

    private final RestClient restClient;
    @Value("${openai.chat.api-key:${OPENAI_API_KEY:}}")
    private String apiKey;
    @Value("${openai.chat.model:gpt-5.4-nano}")
    private String model;

    @Override
    public Optional<String> generateAnswer(String question, List<Product> products) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OpenAI chat api key is not configured");
        }

        ChatCompletionResponse response = restClient.post()
                .uri(CHAT_COMPLETIONS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .body(new ChatCompletionRequest(
                        model,
                        List.of(
                                new Message("system", """
                                        너는 상품 추천 도우미다.
                                        반드시 제공된 상품 목록만 근거로 답변해라.
                                        목록에 없는 정보는 추측하지 말고 없다고 말해라.
                                        답변은 짧고 한국어로 작성해라.
                                        """),
                                new Message("user", buildPrompt(question, products))
                        )
                ))
                .retrieve()
                .body(ChatCompletionResponse.class);

        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            return Optional.empty();
        }

        Message message = response.choices().get(0).message();
        if (message == null || message.content() == null || message.content().isBlank()) {
            return Optional.empty();
        }

        return Optional.of(message.content().trim());
    }

    private String buildPrompt(String question, List<Product> products) {
        StringBuilder builder = new StringBuilder();
        builder.append("질문:\n").append(question).append("\n\n");
        builder.append("상품 목록:\n");
        for (int index = 0; index < products.size(); index++) {
            Product product = products.get(index);
            builder.append(index + 1)
                    .append(". 이름: ").append(product.getName())
                    .append(", 설명: ").append(blankToDash(product.getDescription()))
                    .append(", 가격: ").append(formatPrice(product.getPrice()))
                    .append(", 재고: ").append(product.getStock())
                    .append(", 상태: ").append(product.getStatus())
                    .append('\n');
        }
        builder.append("\n위 상품만 보고 추천 또는 요약 답변을 작성해라.");
        return builder.toString();
    }

    private String blankToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String formatPrice(BigDecimal price) {
        return price == null ? "-" : price.toPlainString();
    }

    private record ChatCompletionRequest(
            String model,
            List<Message> messages
    ) {
    }

    private record ChatCompletionResponse(
            List<Choice> choices
    ) {
    }

    private record Choice(
            Message message
    ) {
    }

    private record Message(
            String role,
            String content
    ) {
    }
}
