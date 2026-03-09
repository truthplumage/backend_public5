package com.grepp.backend5.payment.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "토스 결제 승인 API 응답")
public record TossPaymentResponse(
        @Schema(description = "토스 결제 키")
        String paymentKey,
        @Schema(description = "주문 ID")
        String orderId,
        @JsonProperty("totalAmount")
        @Schema(description = "총 결제 금액")
        Long totalAmount,
        @Schema(description = "결제 수단")
        String method,
        @Schema(description = "결제 상태")
        String status,
        @Schema(description = "결제 요청 시각")
        OffsetDateTime requestedAt,
        @Schema(description = "결제 승인 시각")
        OffsetDateTime approvedAt
) {
}
