package com.grepp.backend5.payment.domain.repository;

import com.grepp.backend5.payment.domain.model.PaymentFailure;

public interface PaymentFailureRepository {

    PaymentFailure save(PaymentFailure failure);
}
