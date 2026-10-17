package com.example.project1.repository;

import com.example.project1.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrder_OrderId(Long orderId);

    List<Payment> findByPaymentStatus(String paymentStatus);

    Optional<Payment> findByTransactionId(String transactionId);
}
