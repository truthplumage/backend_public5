package com.grepp.backend5.product.infrastructure.llm;

import com.grepp.backend5.product.application.llm.ProductLlmAnswerGenerator;
import com.grepp.backend5.product.domain.model.Product;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@ConditionalOnProperty(prefix = "openai.chat", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoOpProductLlmAnswerGenerator implements ProductLlmAnswerGenerator {

    @Override
    public Optional<String> generateAnswer(String question, List<Product> products) {
        return Optional.empty();
    }
}
