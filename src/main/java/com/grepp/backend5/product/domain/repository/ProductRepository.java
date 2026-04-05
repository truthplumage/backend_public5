package com.grepp.backend5.product.domain.repository;

import com.grepp.backend5.product.domain.model.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(UUID productId);

    List<Product> findAll();

    List<Product> findSimilarByEmbedding(float[] embedding, int size);

    void delete(Product product);
}
