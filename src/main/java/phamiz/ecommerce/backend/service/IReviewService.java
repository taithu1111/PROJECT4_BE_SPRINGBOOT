package phamiz.ecommerce.backend.service;

import phamiz.ecommerce.backend.dto.Review.ReviewRequest;
import phamiz.ecommerce.backend.exception.ProductException;
import phamiz.ecommerce.backend.model.Review;
import phamiz.ecommerce.backend.model.User;

import java.util.List;

public interface IReviewService {
    public Review createReview(ReviewRequest request, User user) throws ProductException;

    public List<Review> getAllReview(Long productId);

    public org.springframework.data.domain.Page<Review> getAllReviews(Long productId, Long userId, Integer page,
            Integer size);
}
