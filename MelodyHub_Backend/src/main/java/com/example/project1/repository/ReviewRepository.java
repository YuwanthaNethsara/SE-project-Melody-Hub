package com.example.project1.repository;

import com.example.project1.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProduct_ProductIdAndStatusOrderByCreatedAtDesc(Long productId, String status);

    List<Review> findByProduct_ProductId(Long productId);

    List<Review> findByUser_IdOrderByCreatedAtDesc(Long userId);

    List<Review> findByUser_Id(Long userId);

    List<Review> findByStatus(String status);

    Optional<Review> findByProduct_ProductIdAndUser_Id(Long productId, Long userId);

    long countByStatus(String status);
}
