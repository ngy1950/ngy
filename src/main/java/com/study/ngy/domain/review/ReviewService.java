package com.study.ngy.domain.review;

import com.study.ngy.domain.gallery.GalleryPost;
import com.study.ngy.domain.gallery.GalleryPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final GalleryPostRepository postRepository;

    public List<Review> findApprovedByPostId(Long postId) {
        return reviewRepository.findByPostIdAndApprovedTrueOrderByCreatedAtDesc(postId);
    }

    public List<Review> findPendingReviews() {
        return reviewRepository.findByApprovedFalseOrderByCreatedAtDesc();
    }

    @Transactional
    public void addReview(Long postId, String authorName, int rating, String content) {
        GalleryPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다: " + postId));
        Review review = new Review();
        review.setPost(post);
        review.setAuthorName(authorName);
        review.setRating(rating);
        review.setContent(content);
        review.setApproved(true); // 승인 없이 바로 게시 (승인 기능 비활성화 중)
        reviewRepository.save(review);
    }

    // 반환: postId → [avgRating, reviewCount]
    public Map<Long, double[]> getRatingStats() {
        Map<Long, double[]> result = new HashMap<>();
        for (Object[] row : reviewRepository.findRatingStatsByPost()) {
            result.put((Long) row[0], new double[]{(Double) row[1], ((Long) row[2]).doubleValue()});
        }
        return result;
    }

    @Transactional
    public void approve(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다: " + reviewId));
        review.setApproved(true);
    }

    @Transactional
    public void delete(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }
}
