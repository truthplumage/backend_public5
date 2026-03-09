package com.grepp.backend5.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "\"payment\"", schema = "public")
public class Payment {

    @Id
    private UUID id;

    @Column(name = "payment_key", nullable = false, unique = true, length = 200)
    private String paymentKey;

    @Column(name = "order_id", nullable = false, length = 100)
    private String orderId;

    @Column(name = "total_amount", nullable = false)
    private Long amount;

    @Column(name = "method", length = 50)
    private String method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "fail_reason")
    private String failReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Payment() {
    }

    private Payment(String paymentKey, String orderId, Long amount) {
        this.id = UUID.randomUUID();
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.amount = amount;
        this.status = PaymentStatus.READY;
    }

    public static Payment create(String paymentKey, String orderId, Long amount) {
        return new Payment(paymentKey, orderId, amount);
    }

    public void markConfirmed(String method, LocalDateTime approvedAt, LocalDateTime requestedAt) {
        this.status = PaymentStatus.CONFIRMED;
        this.method = method;
        this.approvedAt = approvedAt;
        this.requestedAt = requestedAt;
        this.failReason = null;
    }

    public void markFailed(String failReason) {
        this.status = PaymentStatus.FAILED;
        this.failReason = failReason;
    }

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = PaymentStatus.READY;
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public String getOrderId() {
        return orderId;
    }

    public Long getAmount() {
        return amount;
    }

    public String getMethod() {
        return method;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public String getFailReason() {
        return failReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
