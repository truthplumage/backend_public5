package com.grepp.backend5.payment.infrastructure;

import com.grepp.backend5.payment.domain.model.PaymentFailure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentFailureJpaRepository extends JpaRepository<PaymentFailure, UUID> {
}
