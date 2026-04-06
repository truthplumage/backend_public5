package com.grepp.backend5.product.application.vector;

import com.grepp.backend5.product.domain.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductEmbeddingService {

    private final ProductEmbeddingGenerator productEmbeddingGenerator;

    public boolean refreshEmbedding(Product product) {
        try {
            Optional<float[]> embedding = productEmbeddingGenerator.generate(buildText(product));
            embedding.ifPresent(product::updateEmbedding);
            return embedding.isPresent();
        } catch (Exception exception) {
            log.warn("Failed to refresh embedding for product {}", product.getId(), exception);
            return false;
        }
    }
    private String buildText(Product product) {
        String description = product.getDescription() == null ? "" : product.getDescription().trim();
        return """
                상품명: %s
                설명: %s
                """.formatted(product.getName(), description);
    }
    public Optional<float[]> generateQueryEmbedding(String query) {
        try {
            return productEmbeddingGenerator.generate(query);
        } catch (Exception exception) {
            log.warn("Failed to generate query embedding", exception);
            return Optional.empty();
        }
    }


}
