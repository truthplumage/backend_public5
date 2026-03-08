package com.grepp.backend5.order.domain.repository;

import com.grepp.backend5.order.domain.model.Order;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {

    List<Order> findAll();

    Optional<Order> findById(UUID id);

    Order save(Order order);

    List<Order> findUnsettledPaidOrders(LocalDateTime fromInclusive, LocalDateTime toExclusive);
}
