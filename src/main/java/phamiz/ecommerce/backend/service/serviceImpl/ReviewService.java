package phamiz.ecommerce.backend.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import phamiz.ecommerce.backend.dto.Review.ReviewRequest;
import phamiz.ecommerce.backend.dto.Review.UpdateReviewRequest;
import phamiz.ecommerce.backend.exception.ProductException;
import phamiz.ecommerce.backend.model.Product;
import phamiz.ecommerce.backend.model.Review;
import phamiz.ecommerce.backend.model.User;
import phamiz.ecommerce.backend.repositories.IProductRepository;
import phamiz.ecommerce.backend.repositories.IReviewRepository;
import phamiz.ecommerce.backend.service.IProductService;
import phamiz.ecommerce.backend.service.IReviewService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService implements IReviewService {

    private final IReviewRepository reviewRepository;
    private final IProductService productService;
    private final IProductRepository productRepository;

    @Override
    public Review createReview(ReviewRequest request, User user) throws ProductException {
        Product product = productService.findProductById(request.getProductId());

        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setReview(request.getReview());
        review.setCreatedAt(LocalDateTime.now());
        return reviewRepository.save(review);
    }

    @Override
    public List<Review> getAllReview(Long productId) {
        return reviewRepository.getAllProductsReview(productId);
    }

    @Override
    public org.springframework.data.domain.Page<Review> getAllReviews(Long productId, Long userId, Integer page,
            Integer size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return reviewRepository.findByFilter(productId, userId, pageable);
    }

    @Override
    public Review updateReview(Long reviewId, UpdateReviewRequest request, User user) throws Exception {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new Exception("Review not found with id: " + reviewId));

        // Authorization check: user can only update their own review
        if (!review.getUser().getId().equals(user.getId())) {
            throw new Exception("You are not authorized to update this review");
        }

        review.setReview(request.getReview());
        review.setCreatedAt(LocalDateTime.now()); // Update timestamp

        return reviewRepository.save(review);
    }

    @Override
    public void deleteReview(Long reviewId, User user) throws Exception {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new Exception("Review not found with id: " + reviewId));

        // Authorization check: user can only delete their own review
        if (!review.getUser().getId().equals(user.getId())) {
            throw new Exception("You are not authorized to delete this review");
        }

        reviewRepository.delete(review);
    }

    @Override
    public void deleteReviewByAdmin(Long reviewId) throws Exception {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new Exception("Review not found with id: " + reviewId));
        reviewRepository.delete(review);
    }
}