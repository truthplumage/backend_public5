package com.grepp.backend5.payment.application.dto;

import com.grepp.backend5.payment.domain.Payment;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 결제 응답 DTO.
 */
public record PaymentInfo(
        UUID id,
        String orderId,
        String paymentKey,
        Long amount,
        com.grepp.backend5.payment.domain.PaymentStatus status,
        String method,
        LocalDateTime requestedAt,
        LocalDateTime approvedAt,
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
