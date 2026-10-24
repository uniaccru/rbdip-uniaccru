package com.rbdip.bookstore.review;

import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Модуль отзывов работает с заказами только через явный контракт проверки
 * покупки, не зная внутреннюю реализацию модуля order.
 */
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final PurchaseVerifier purchaseVerifier;

    public ReviewService(ReviewRepository reviewRepository, PurchaseVerifier purchaseVerifier) {
        this.reviewRepository = reviewRepository;
        this.purchaseVerifier = purchaseVerifier;
    }

    public Review addReview(Long productId, String authorName, Integer rating, String comment) {
        purchaseVerifier.hasPurchasedProduct(productId, authorName);
        Review review = new Review(productId, authorName == null ? "anonymous" : authorName, rating, comment);
        return reviewRepository.save(review);
    }

    public List<Review> listReviews(Long productId) {
        return reviewRepository.findByProductId(productId);
    }
}
