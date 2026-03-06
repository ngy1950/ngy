package com.study.ngy.domain.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByPostIdAndApprovedTrueOrderByCreatedAtDesc(Long postId);

    List<Review> findByApprovedFalseOrderByCreatedAtDesc();

    // [postId, avgRating, reviewCount] 형태로 반환
    @Query("SELECT r.post.id, AVG(r.rating), COUNT(r) FROM Review r WHERE r.approved = true GROUP BY r.post.id")
    List<Object[]> findRatingStatsByPost();
}
