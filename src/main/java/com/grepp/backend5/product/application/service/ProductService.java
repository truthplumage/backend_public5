package com.grepp.backend5.product.application.service;

import com.grepp.backend5.product.application.port.in.ProductUseCase;
import com.grepp.backend5.product.application.port.out.ProductPersistencePort;
import com.grepp.backend5.product.domain.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductService implements ProductUseCase {

    private final ProductPersistencePort productPersistencePort;

    @Override
    @Transactional
    public Product create(Product request) {
        Product product = Product.create(
                request.getSellerId(),
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getStock(),
                request.getStatus(),
                request.getRegId()
        );
        return productPersistencePort.save(product);
    }

    @Override
    public Product getById(UUID productId) {
        return findByIdOrThrow(productId);
    }

    @Override
    public List<Product> getAll() {
        return productPersistencePort.findAll();
    }

    @Override
    @Transactional
    public Product update(UUID productId, Product request) {
        Product product = findByIdOrThrow(productId);
        product.update(
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getStock(),
                request.getStatus(),
                request.getModifyId()
        );
        return product;
    }

    @Override
    @Transactional
    public void delete(UUID productId) {
        Product product = findByIdOrThrow(productId);
        productPersistencePort.delete(product);
    }

    private Product findByIdOrThrow(UUID productId) {
        return productPersistencePort.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."));
    }
}
