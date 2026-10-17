package com.example.project1.service;

import com.example.project1.dto.PaymentRequest;
import com.example.project1.dto.PaymentResponse;
import com.example.project1.entity.Order;
import com.example.project1.entity.Payment;
import com.example.project1.repository.OrderRepository;
import com.example.project1.repository.PaymentRepository;
import com.example.project1.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository,
            UserRepository userRepository) {

        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public PaymentResponse createPayment(
            PaymentRequest request) {

        if (request.getOrderId() == null) {
            throw new IllegalArgumentException("Order is required");
        }

        if (request.getPaymentMethod() == null ||
                request.getPaymentMethod().trim().isEmpty()) {
            throw new IllegalArgumentException("Please choose a payment method");
        }

        if (request.getPaymentMethod().trim().length() > 50) {
            throw new IllegalArgumentException("Payment method is invalid");
        }

        Order order = orderRepository
                .findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if ("CANCELLED".equalsIgnoreCase(order.getOrderStatus())) {
            throw new IllegalStateException("A cancelled order cannot be paid");
        }

        if (paymentRepository
                .findByOrder_OrderId(request.getOrderId())
                .isPresent()) {
            throw new IllegalStateException("This order has already been paid");
        }

        if (order.getTotalAmount() == null || order.getTotalAmount() <= 0) {
            throw new IllegalStateException("This order has nothing to pay");
        }

        // The amount ALWAYS comes from the order stored in the database,
        // never from the request.
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentMethod(request.getPaymentMethod().trim());

        // Simulated gateway: the payment succeeds immediately.
        // (Replace this block with a real gateway call when one is integrated.)
        payment.setPaymentStatus("PAID");
        payment.setTransactionId("TXN-" + UUID.randomUUID());

        Payment savedPayment =
                paymentRepository.save(payment);

        order.setPaymentStatus("PAID");

        if ("PENDING".equals(order.getOrderStatus())) {
            order.confirm(); // STATE pattern: a paid PENDING order moves to CONFIRMED
        }

        orderRepository.save(order);

        return convertToResponse(savedPayment);
    }

    @Override
    @Transactional
    public PaymentResponse createPaymentForUser(
            PaymentRequest request, String email, boolean admin) {

        if (request.getOrderId() == null) {
            throw new IllegalArgumentException("Order is required");
        }

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // a customer can only pay for their OWN order
        if (!admin && !order.getUser().getEmail().equalsIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You cannot pay for another user's order");
        }

        return createPayment(request);
    }

    @Override
    public List<PaymentResponse> getPaymentsByUser(Long userId) {
        return paymentRepository.findAll().stream()
                .filter(p -> p.getOrder() != null && p.getOrder().getUser() != null
                        && userId.equals(p.getOrder().getUser().getId()))
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean orderBelongsToUser(Long orderId, Long userId) {
        return orderRepository.findById(orderId)
                .map(o -> o.getUser() != null && userId.equals(o.getUser().getId()))
                .orElse(false);
    }

    @Override
    public List<PaymentResponse> getAllPayments() {

        return paymentRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentResponse getPaymentById(Long id) {

        Payment payment =
                paymentRepository.findById(id)
                        .orElse(null);

        if (payment == null) {
            return null;
        }

        return convertToResponse(payment);
    }

    @Override
    public PaymentResponse getPaymentByOrder(
            Long orderId) {

        Payment payment =
                paymentRepository
                        .findByOrder_OrderId(orderId)
                        .orElse(null);

        if (payment == null) {
            return null;
        }

        return convertToResponse(payment);
    }

    @Override
    public List<PaymentResponse> getPaymentsByStatus(
            String status) {

        return paymentRepository
                .findByPaymentStatus(status)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentResponse updatePaymentStatus(
            Long id,
            String status) {

        Payment payment =
                paymentRepository.findById(id)
                        .orElse(null);

        if (payment == null) {
            return null;
        }

        if (status == null ||
                !java.util.Set.of("PENDING", "PAID", "FAILED", "REFUNDED")
                        .contains(status.trim().toUpperCase())) {
            throw new IllegalArgumentException("Unknown payment status: " + status);
        }

        status = status.trim().toUpperCase();

        payment.setPaymentStatus(status);

        Payment updated =
                paymentRepository.save(payment);

        // Update related order
        Order order = payment.getOrder();

        order.setPaymentStatus(status);

        if ("PAID".equals(status) &&
                "PENDING".equals(order.getOrderStatus())) {

            order.confirm(); // STATE pattern
        }

        orderRepository.save(order);

        return convertToResponse(updated);
    }

    @Override
    @Transactional
    public String deletePayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found."));
        Order order = payment.getOrder();
        paymentRepository.delete(payment);
        // the order is no longer paid; a cancelled order keeps its own payment status
        if (order != null && !"CANCELLED".equalsIgnoreCase(order.getOrderStatus())) {
            order.setPaymentStatus("PENDING");
            orderRepository.save(order);
        }
        return "Payment deleted successfully";
    }

    private PaymentResponse convertToResponse(
            Payment payment) {

        return new PaymentResponse(

                payment.getPaymentId(),

                payment.getOrder().getOrderId(),

                payment.getAmount(),

                payment.getPaymentDate(),

                payment.getPaymentMethod(),

                payment.getPaymentStatus(),

                payment.getTransactionId()
        );
    }
}
