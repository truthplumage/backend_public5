package com.grepp.backend5.product.infrastructure.persistence;

import com.grepp.backend5.product.domain.model.Product;
import com.grepp.backend5.product.domain.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ProductRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;
    private final EntityManager entityManager;

    public ProductRepositoryAdapter(ProductJpaRepository productJpaRepository,
                                    EntityManager entityManager) {
        this.productJpaRepository = productJpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    public Product save(Product product) {
        return productJpaRepository.save(product);
    }

    @Override
    public Optional<Product> findById(UUID productId) {
        return productJpaRepository.findById(productId);
    }

    @Override
    public List<Product> findAll() {
        return productJpaRepository.findAll();
    }

    @Override
    public List<Product> findSimilarByEmbedding(float[] embedding, int size) {
        String sql = """
                SELECT *
                FROM public."product" p
                WHERE p.embedding IS NOT NULL
                ORDER BY p.embedding <=> CAST(:embedding AS vector)
                """;
        return entityManager.createNativeQuery(sql, Product.class)
                .setParameter("embedding", toVectorLiteral(embedding))
                .setMaxResults(size)
                .getResultList();
    }

    @Override
    public void delete(Product product) {
        productJpaRepository.delete(product);
    }

    private String toVectorLiteral(float[] embedding) {
        StringBuilder builder = new StringBuilder("[");
        for (int index = 0; index < embedding.length; index++) {
            if (index > 0) {
                builder.append(',');
            }
            builder.append(embedding[index]);
        }
        builder.append(']');
        return builder.toString();
    }
}
