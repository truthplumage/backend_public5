package com.grepp.backend5.product.infrastructure.persistence;

import com.grepp.backend5.product.domain.model.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProductJpaRepository extends JpaRepository<Product, UUID> {

    @Query(value = """
    SELECT *
    FROM public.product p
    WHERE p.embedding IS NOT NULL
    ORDER BY p.embedding <=> CAST(:embedding AS vector)
    """, nativeQuery = true)
    List<Product> findSimilarByEmbedding(@Param("embedding") String embedding, Pageable pageable);
}
