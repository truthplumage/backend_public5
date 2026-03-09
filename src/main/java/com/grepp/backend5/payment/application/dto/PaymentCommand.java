package com.grepp.backend5.payment.application.dto;

/**
 * 결제 확인에 필요한 입력 데이터를 묶는 명령 DTO.
 */
public record PaymentCommand(
        String paymentKey,
        String orderId,
        Long amount
) {
}
