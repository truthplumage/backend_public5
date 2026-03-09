package com.grepp.backend5.payment.domain.repository;

import com.grepp.backend5.payment.domain.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {

    Page<Payment> findAll(Pageable pageable);

    Optional<Payment> findById(UUID id);

    Payment save(Payment payment);
}
