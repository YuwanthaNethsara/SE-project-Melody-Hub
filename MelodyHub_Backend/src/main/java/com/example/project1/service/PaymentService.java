package com.example.project1.service;

import com.example.project1.dto.PaymentRequest;
import com.example.project1.dto.PaymentResponse;

import java.util.List;

public interface PaymentService {
    PaymentResponse createPayment(PaymentRequest request);
    PaymentResponse createPaymentForUser(PaymentRequest request, String email, boolean admin);
    List<PaymentResponse> getAllPayments();
    List<PaymentResponse> getPaymentsByUser(Long userId);
    PaymentResponse getPaymentById(Long id);
    PaymentResponse getPaymentByOrder(Long orderId);
    boolean orderBelongsToUser(Long orderId, Long userId);
    List<PaymentResponse> getPaymentsByStatus(String status);
    PaymentResponse updatePaymentStatus(Long id, String status);
    String deletePayment(Long id);
}
