package com.example.project1.service;

import com.example.project1.dto.DashboardStatsResponse;
import com.example.project1.dto.OrderResponse;
import com.example.project1.entity.Advertisement;
import com.example.project1.entity.Order;
import com.example.project1.entity.Payment;
import com.example.project1.entity.Review;
import com.example.project1.enums.Role;
import com.example.project1.enums.StockStatus;
import com.example.project1.enums.WarrantyClaimStatus;
import com.example.project1.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/** Builds the admin dashboard figures from the real tables (nothing is estimated). */
@Service
public class DashboardServiceImpl implements DashboardService {

    private static final List<String> ORDER_STATUSES =
            List.of("PENDING", "CONFIRMED", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED");

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final DigitalMusicRepository digitalMusicRepository;
    private final AdvertisementRepository advertisementRepository;
    private final InventoryRepository inventoryRepository;
    private final WarrantyClaimRepository warrantyClaimRepository;
    private final PromotionRepository promotionRepository;
    private final ReviewRepository reviewRepository;

    public DashboardServiceImpl(UserRepository userRepository,
                                ProductRepository productRepository,
                                OrderRepository orderRepository,
                                PaymentRepository paymentRepository,
                                DigitalMusicRepository digitalMusicRepository,
                                AdvertisementRepository advertisementRepository,
                                InventoryRepository inventoryRepository,
                                WarrantyClaimRepository warrantyClaimRepository,
                                PromotionRepository promotionRepository,
                                ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.digitalMusicRepository = digitalMusicRepository;
        this.advertisementRepository = advertisementRepository;
        this.inventoryRepository = inventoryRepository;
        this.warrantyClaimRepository = warrantyClaimRepository;
        this.promotionRepository = promotionRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getAdminStats() {
        DashboardStatsResponse r = new DashboardStatsResponse();
        LocalDate today = LocalDate.now();

        long users = userRepository.count();
        long customers = userRepository.findByRoleIn(List.of(Role.CUSTOMER)).size();
        r.setTotalUsers(users);
        r.setCustomers(customers);
        r.setStaff(users - customers);

        r.setTotalProducts(productRepository.count());
        r.setActiveProducts(productRepository.findAll().stream().filter(StockRules::isSellable).count());

        List<Order> orders = orderRepository.findAll();
        r.setTotalOrders(orders.size());
        Map<String, Long> byStatus = new LinkedHashMap<>();
        ORDER_STATUSES.forEach(s -> byStatus.put(s, 0L));
        for (Order o : orders) {
            String status = o.getOrderStatus() == null ? "PENDING" : o.getOrderStatus().toUpperCase(Locale.ROOT);
            byStatus.merge(status, 1L, Long::sum);
        }
        r.setOrdersByStatus(byStatus);
        r.setPendingOrders(byStatus.getOrDefault("PENDING", 0L));
        // orders that still wait for money (cancelled orders are not counted)
        r.setPendingPayments(orders.stream()
                .filter(o -> !"CANCELLED".equalsIgnoreCase(o.getOrderStatus()))
                .filter(o -> o.getPaymentStatus() == null || "PENDING".equalsIgnoreCase(o.getPaymentStatus()))
                .count());

        List<Payment> paid = paymentRepository.findByPaymentStatus("PAID");
        r.setTotalRevenue(round2(paid.stream().mapToDouble(p -> p.getAmount() == null ? 0 : p.getAmount()).sum()));
        r.setRevenueByMonth(revenueByMonth(paid, YearMonth.from(today)));

        r.setDigitalMusic(digitalMusicRepository.count());
        r.setActiveDigitalMusic(digitalMusicRepository.findAll().stream()
                .filter(m -> m.getStatus() == null || "ACTIVE".equalsIgnoreCase(m.getStatus())
                        || "AVAILABLE".equalsIgnoreCase(m.getStatus()))
                .count());

        List<Advertisement> activeAds = advertisementRepository.findByStatus("ACTIVE");
        r.setActiveAdvertisements(activeAds.stream()
                .filter(a -> a.getCampaign() != null
                        && (a.getCampaign().getStartDate() == null || !today.isBefore(a.getCampaign().getStartDate()))
                        && (a.getCampaign().getEndDate() == null || !today.isAfter(a.getCampaign().getEndDate())))
                .count());

        r.setLowStockProducts(inventoryRepository.findByStockStatus(StockStatus.LOW_STOCK).size()
                + inventoryRepository.findByStockStatus(StockStatus.OUT_OF_STOCK).size());
        r.setPendingWarrantyClaims(warrantyClaimRepository.findByStatus(WarrantyClaimStatus.PENDING).size());
        r.setActivePromotions(promotionRepository.findAll().stream()
                .filter(p -> (p.getStartDate() == null || !today.isBefore(p.getStartDate()))
                        && (p.getEndDate() == null || !today.isAfter(p.getEndDate())))
                .count());

        List<Review> reviews = reviewRepository.findAll();
        r.setTotalReviews(reviews.size());
        r.setAverageRating(Math.round(reviews.stream().mapToInt(Review::getRating).average().orElse(0) * 10) / 10.0);

        r.setRecentOrders(orders.stream()
                .sorted(Comparator.comparing(Order::getOrderDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(6)
                .map(o -> new OrderResponse(o.getOrderId(), o.getUser().getId(), o.getUser().getName(),
                        o.getUser().getEmail(), o.getOrderDate(), o.getTotalAmount(), o.getOrderStatus(),
                        o.getPaymentStatus(), o.getShippingAddress()))
                .collect(Collectors.toList()));
        return r;
    }

    /** Paid revenue of the last six months, oldest first (months without sales show 0). */
    private static List<DashboardStatsResponse.MonthlyAmount> revenueByMonth(List<Payment> paid, YearMonth current) {
        Map<YearMonth, Double> sums = new LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) {
            sums.put(current.minusMonths(i), 0.0);
        }
        for (Payment p : paid) {
            if (p.getPaymentDate() == null || p.getAmount() == null) {
                continue;
            }
            YearMonth month = YearMonth.from(p.getPaymentDate());
            if (sums.containsKey(month)) {
                sums.merge(month, p.getAmount(), Double::sum);
            }
        }
        List<DashboardStatsResponse.MonthlyAmount> out = new ArrayList<>();
        sums.forEach((month, amount) -> out.add(new DashboardStatsResponse.MonthlyAmount(month.toString(), round2(amount))));
        return out;
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
