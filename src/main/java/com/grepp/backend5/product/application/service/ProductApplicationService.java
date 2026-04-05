package com.grepp.backend5.product.application.service;

import com.grepp.backend5.product.application.acl.SellerAcl;
import com.grepp.backend5.product.application.acl.SellerIdentity;
import com.grepp.backend5.product.application.exception.ProductNotFoundException;
import com.grepp.backend5.product.application.usecase.ProductUseCase;
import com.grepp.backend5.product.application.vector.ProductEmbeddingService;
import com.grepp.backend5.product.domain.model.Product;
import com.grepp.backend5.product.domain.repository.ProductRepository;
import com.grepp.backend5.product.presentation.dto.request.CreateProductRequest;
import com.grepp.backend5.product.presentation.dto.request.UpdateProductRequest;
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
