package com.grepp.backend5.payment.application.dto;

import com.grepp.backend5.payment.domain.model.Payment;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 결제 응답 DTO.
 */
@Schema(description = "결제 응답")
public record PaymentInfo(
        @Schema(description = "결제 ID")
        UUID id,
        @Schema(description = "주문 ID")
        String orderId,
        @Schema(description = "토스 결제 키")
        String paymentKey,
        @Schema(description = "결제 금액")
        Long amount,
        @Schema(description = "결제 상태")
        com.grepp.backend5.payment.domain.PaymentStatus status,
        @Schema(description = "결제 수단")
        String method,
        @Schema(description = "결제 요청 시각")
        LocalDateTime requestedAt,
        @Schema(description = "결제 승인 시각")
        LocalDateTime approvedAt,
        @Schema(description = "실패 사유")
        String failReason
) {

    public static PaymentInfo from(Payment payment) {
        return new PaymentInfo(
                payment.getId(),
                payment.getOrderId(),
                payment.getPaymentKey(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getMethod(),
                payment.getRequestedAt(),
                payment.getApprovedAt(),
                payment.getFailReason()
        );
    }
}
