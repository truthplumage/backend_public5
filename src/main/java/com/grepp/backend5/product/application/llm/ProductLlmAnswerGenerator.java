package com.grepp.backend5.product.application.llm;

import com.grepp.backend5.product.domain.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductLlmAnswerGenerator {

    Optional<String> generateAnswer(String question, List<Product> products);
}
