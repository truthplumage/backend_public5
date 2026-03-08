package com.grepp.backend5.order.infrastructure.persistence;

import com.grepp.backend5.order.domain.model.Order;
import com.grepp.backend5.order.domain.repository.OrderRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;

    @Override
    public List<Order> findAll() {
        return orderJpaRepository.findAll();
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return orderJpaRepository.findById(id);
    }

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(order);
    }

    @Override
    public List<Order> findUnsettledPaidOrders(LocalDateTime fromInclusive, LocalDateTime toExclusive) {
        return orderJpaRepository.findByStatusAndSettledFalseAndPaidAtGreaterThanEqualAndPaidAtLessThan(
                "PAID",
                fromInclusive,
                toExclusive
        );
    }
}
