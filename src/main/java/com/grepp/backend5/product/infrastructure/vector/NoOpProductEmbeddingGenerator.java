package com.grepp.backend5.product.infrastructure.vector;

import com.grepp.backend5.product.application.vector.ProductEmbeddingGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@ConditionalOnProperty(prefix = "openai.embedding", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoOpProductEmbeddingGenerator implements ProductEmbeddingGenerator {

    @Override
    public Optional<float[]> generate(String text) {
        return Optional.empty();
    }
}
