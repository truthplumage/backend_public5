package com.grepp.backend5.product.application.usecase;

import com.grepp.backend5.product.domain.model.Product;
import com.grepp.backend5.product.presentation.dto.request.CreateProductRequest;
import com.grepp.backend5.product.presentation.dto.request.UpdateProductRequest;
import com.grepp.backend5.product.presentation.dto.response.ProductEmbeddingRefreshResponse;

import java.util.List;
import java.util.UUID;

public interface ProductUseCase {

    Product create(CreateProductRequest request, UUID actorId);

    Product getById(UUID productId);

    List<Product> getAll();

    List<Product> searchBySemantic(String query, int size);

    ProductEmbeddingRefreshResponse refreshAllEmbeddings();

    Product update(UUID productId, UpdateProductRequest request, UUID actorId);

    void delete(UUID productId);
}
