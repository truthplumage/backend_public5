package com.grepp.backend5.order.presentation.controller;

import com.grepp.backend5.order.application.usecase.OrderUseCase;
import com.grepp.backend5.order.presentation.dto.request.CreateOrderRequest;
import com.grepp.backend5.order.presentation.dto.response.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "주문 정보 API")
public class OrderController {

    private final OrderUseCase orderUseCase;

    @PostMapping
    @Operation(summary = "주문 생성", description = "정산 대상 주문 정보를 생성합니다.")
    public ResponseEntity<OrderResponse> create(@RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderUseCase.create(request));
    }

    @GetMapping
    @Operation(summary = "주문 전체 조회", description = "현재 저장된 주문 정보를 모두 조회합니다.")
    public ResponseEntity<List<OrderResponse>> findAll() {
        return ResponseEntity.ok(orderUseCase.findAll());
    }

    @GetMapping("/settlement-candidates")
    @Operation(summary = "정산 대상 주문 조회", description = "정산일 기준 미정산(PAID) 주문을 조회합니다.")
    public ResponseEntity<List<OrderResponse>> findSettlementCandidates(
            @Parameter(description = "정산일(yyyy-MM-dd), 미입력 시 오늘")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate settlementDate
    ) {
        return ResponseEntity.ok(orderUseCase.findSettlementCandidates(settlementDate));
    }
}
