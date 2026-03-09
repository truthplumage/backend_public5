package com.grepp.backend5.payment.application;

import com.grepp.backend5.payment.application.dto.PaymentCommand;
import com.grepp.backend5.payment.application.dto.PaymentFailCommand;
import com.grepp.backend5.payment.application.dto.PaymentFailureInfo;
import com.grepp.backend5.payment.application.dto.PaymentInfo;
import com.grepp.backend5.payment.client.TossPaymentClient;
import com.grepp.backend5.payment.client.dto.TossPaymentResponse;
import com.grepp.backend5.payment.domain.model.Payment;
import com.grepp.backend5.payment.domain.model.PaymentFailure;
import com.grepp.backend5.payment.domain.repository.PaymentFailureRepository;
import com.grepp.backend5.payment.domain.repository.PaymentRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentFailureRepository paymentFailureRepository;
    private final TossPaymentClient tossPaymentClient;

    public ResponseEntity<List<PaymentInfo>> findAll(Pageable pageable) {
        Page<Payment> page = paymentRepository.findAll(pageable);
        List<PaymentInfo> payments = page.stream()
                .map(PaymentInfo::from)
                .toList();
        return ResponseEntity.status(HttpStatus.OK).body(payments);
    }

    public ResponseEntity<PaymentInfo> confirm(PaymentCommand command) {
        TossPaymentResponse tossPayment = tossPaymentClient.confirm(command);
//        UUID orderId = UUID.fromString(tossPayment.orderId());
//        PurchaseOrder order = orderService.findEntity(orderId);
        Payment payment = Payment.create(
                tossPayment.paymentKey(),
                tossPayment.orderId(),
                tossPayment.totalAmount()
        );
        LocalDateTime approvedAt = tossPayment.approvedAt() != null ? tossPayment.approvedAt().toLocalDateTime() : null;
        LocalDateTime requestedAt = tossPayment.requestedAt() != null ? tossPayment.requestedAt().toLocalDateTime() : null;

        payment.markConfirmed(tossPayment.method(), approvedAt, requestedAt);

        Payment saved = paymentRepository.save(payment);
        return ResponseEntity.status(HttpStatus.CREATED).body(PaymentInfo.from(saved));
    }

    public ResponseEntity<PaymentFailureInfo> recordFailure(PaymentFailCommand command) {
        PaymentFailure failure = PaymentFailure.from(
                command.orderId(),
                command.paymentKey(),
                command.errorCode(),
                command.errorMessage(),
                command.amount(),
                command.rawPayload()
        );
        PaymentFailure saved = paymentFailureRepository.save(failure);
        return ResponseEntity.status(HttpStatus.CREATED).body(PaymentFailureInfo.from(saved));
    }
}
