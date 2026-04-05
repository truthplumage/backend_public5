package com.grepp.backend5.product.application.service;

import com.grepp.backend5.product.application.acl.SellerAcl;
import com.grepp.backend5.product.application.acl.SellerIdentity;
import com.grepp.backend5.product.application.exception.ProductNotFoundException;
import com.grepp.backend5.product.application.llm.ProductLlmAnswerGenerator;
import com.grepp.backend5.product.application.usecase.ProductUseCase;
import com.grepp.backend5.product.application.vector.ProductEmbeddingService;
import com.grepp.backend5.product.domain.model.Product;
import com.grepp.backend5.product.domain.repository.ProductRepository;
import com.grepp.backend5.product.presentation.dto.request.CreateProductRequest;
import com.grepp.backend5.product.presentation.dto.request.ProductLlmSearchRequest;
import com.grepp.backend5.product.presentation.dto.request.UpdateProductRequest;
import com.grepp.backend5.product.presentation.dto.response.ProductEmbeddingRefreshResponse;
import com.grepp.backend5.product.presentation.dto.response.ProductLlmSearchResponse;
import com.grepp.backend5.product.presentation.dto.response.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductApplicationService implements ProductUseCase {

    private final SellerAcl sellerAcl;
    private final ProductRepository productRepository;
    private final ProductEmbeddingService productEmbeddingService;
    private final ProductLlmAnswerGenerator productLlmAnswerGenerator;

    @Override
    @Transactional
    public Product create(CreateProductRequest request, UUID actorId) {
        SellerIdentity seller = sellerAcl.loadActiveSeller(request.sellerId());
        Product product = Product.create(
                seller.id(),
                request.name(),
                request.description(),
                request.price(),
                request.stock(),
                request.status(),
                actorId
        );
        productEmbeddingService.refreshEmbedding(product);
        return productRepository.save(product);
    }

    @Override
    public Product getById(UUID productId) {
        return findByIdOrThrow(productId);
    }

    @Override
    public List<Product> getAll() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> searchBySemantic(String query, int size) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        int limitedSize = Math.max(1, Math.min(size, 20));
        return productEmbeddingService.generateQueryEmbedding(query)
                .map(embedding -> productRepository.findSimilarByEmbedding(embedding, limitedSize))
                .orElse(List.of());
    }

    @Override
    @Transactional
    public ProductEmbeddingRefreshResponse refreshAllEmbeddings() {
        List<Product> products = productRepository.findAll();
        int updatedCount = 0;

        for (Product product : products) {
            if (productEmbeddingService.refreshEmbedding(product)) {
                updatedCount++;
            }
        }

        return new ProductEmbeddingRefreshResponse(products.size(), updatedCount);
    }

    @Override
    public ProductLlmSearchResponse searchWithLlm(ProductLlmSearchRequest request) {
        List<Product> products = searchBySemantic(request.question(), request.resolvedSize());

        if (products.isEmpty()) {
            return new ProductLlmSearchResponse(
                    request.question(),
                    "관련 상품을 찾지 못했습니다.",
                    List.of()
            );
        }

        String answer = productLlmAnswerGenerator.generateAnswer(request.question(), products)
                .orElse("LLM 답변 기능이 꺼져 있어 유사한 상품 목록만 반환합니다.");

        return new ProductLlmSearchResponse(
                request.question(),
                answer,
                products.stream()
                        .map(ProductResponse::from)
                        .toList()
        );
    }

    @Override
    @Transactional
    public Product update(UUID productId, UpdateProductRequest request, UUID actorId) {
        Product product = findByIdOrThrow(productId);
        product.update(
                request.name(),
                request.description(),
                request.price(),
                request.stock(),
                request.status(),
                actorId
        );
        productEmbeddingService.refreshEmbedding(product);
        return product;
    }

    @Override
    @Transactional
    public void delete(UUID productId) {
        Product product = findByIdOrThrow(productId);
        productRepository.delete(product);
    }

    private Product findByIdOrThrow(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }
}
