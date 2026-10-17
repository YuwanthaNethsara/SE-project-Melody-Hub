package com.example.project1.dto;

import java.util.List;
import java.util.Map;

/** Figures for the admin dashboard. Every number is computed from the database. */
public class DashboardStatsResponse {

    /** One month of revenue, e.g. {"month":"2026-09","amount":12500.00}. */
    public record MonthlyAmount(String month, double amount) {
    }

    private long totalUsers;
    private long customers;
    private long staff;
    private long totalProducts;
    private long activeProducts;
    private long totalOrders;
    private long pendingOrders;
    private double totalRevenue;
    private long pendingPayments;
    private long digitalMusic;
    private long activeDigitalMusic;
    private long activeAdvertisements;
    private long lowStockProducts;
    private long pendingWarrantyClaims;
    private long activePromotions;
    private long totalReviews;
    private double averageRating;
    private Map<String, Long> ordersByStatus;
    private List<MonthlyAmount> revenueByMonth;
    private List<OrderResponse> recentOrders;

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long v) { totalUsers = v; }
    public long getCustomers() { return customers; }
    public void setCustomers(long v) { customers = v; }
    public long getStaff() { return staff; }
    public void setStaff(long v) { staff = v; }
    public long getTotalProducts() { return totalProducts; }
    public void setTotalProducts(long v) { totalProducts = v; }
    public long getActiveProducts() { return activeProducts; }
    public void setActiveProducts(long v) { activeProducts = v; }
    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long v) { totalOrders = v; }
    public long getPendingOrders() { return pendingOrders; }
    public void setPendingOrders(long v) { pendingOrders = v; }
    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double v) { totalRevenue = v; }
    public long getPendingPayments() { return pendingPayments; }
    public void setPendingPayments(long v) { pendingPayments = v; }
    public long getDigitalMusic() { return digitalMusic; }
    public void setDigitalMusic(long v) { digitalMusic = v; }
    public long getActiveDigitalMusic() { return activeDigitalMusic; }
    public void setActiveDigitalMusic(long v) { activeDigitalMusic = v; }
    public long getActiveAdvertisements() { return activeAdvertisements; }
    public void setActiveAdvertisements(long v) { activeAdvertisements = v; }
    public long getLowStockProducts() { return lowStockProducts; }
    public void setLowStockProducts(long v) { lowStockProducts = v; }
    public long getPendingWarrantyClaims() { return pendingWarrantyClaims; }
    public void setPendingWarrantyClaims(long v) { pendingWarrantyClaims = v; }
    public long getActivePromotions() { return activePromotions; }
    public void setActivePromotions(long v) { activePromotions = v; }
    public long getTotalReviews() { return totalReviews; }
    public void setTotalReviews(long v) { totalReviews = v; }
    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double v) { averageRating = v; }
    public Map<String, Long> getOrdersByStatus() { return ordersByStatus; }
    public void setOrdersByStatus(Map<String, Long> v) { ordersByStatus = v; }
    public List<MonthlyAmount> getRevenueByMonth() { return revenueByMonth; }
    public void setRevenueByMonth(List<MonthlyAmount> v) { revenueByMonth = v; }
    public List<OrderResponse> getRecentOrders() { return recentOrders; }
    public void setRecentOrders(List<OrderResponse> v) { recentOrders = v; }
}
