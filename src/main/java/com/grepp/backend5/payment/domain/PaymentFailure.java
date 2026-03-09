package com.grepp.backend5.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "\"payment_failure\"", schema = "public")
public class PaymentFailure {

    @Id
    private UUID id;

    @Column(name = "order_id", nullable = false, length = 100)
    private String orderId;

    @Column(name = "payment_key", length = 200)
    private String paymentKey;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "amount")
    private Long amount;

    @Lob
    @Column(name = "raw_payload", columnDefinition = "TEXT")
    private String rawPayload;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected PaymentFailure() {
    }

    private PaymentFailure(String orderId,
                           String paymentKey,
                           String errorCode,
                           String errorMessage,
                           Long amount,
                           String rawPayload) {
        this.id = UUID.randomUUID();
        this.orderId = orderId;
        this.paymentKey = paymentKey;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.amount = amount;
        this.rawPayload = rawPayload;
    }

    public static PaymentFailure from(String orderId,
                                      String paymentKey,
                                      String errorCode,
                                      String errorMessage,
                                      Long amount,
                                      String rawPayload) {
        return new PaymentFailure(orderId, paymentKey, errorCode, errorMessage, amount, rawPayload);
    }

    @PrePersist
    public void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Long getAmount() {
        return amount;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
