package com.grepp.backend5.order.application.usecase;

import com.grepp.backend5.order.presentation.dto.request.CreateOrderRequest;
import com.grepp.backend5.order.presentation.dto.response.OrderResponse;
import java.time.LocalDate;
import java.util.List;

public interface OrderUseCase {

    OrderResponse create(CreateOrderRequest request);

    List<OrderResponse> findAll();

    List<OrderResponse> findSettlementCandidates(LocalDate settlementDate);
}
