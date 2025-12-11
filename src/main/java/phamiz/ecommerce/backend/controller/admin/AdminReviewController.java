package phamiz.ecommerce.backend.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import phamiz.ecommerce.backend.dto.ApiResponse;
import phamiz.ecommerce.backend.dto.Review.ReviewAdminDTO;
import phamiz.ecommerce.backend.model.Review;
import phamiz.ecommerce.backend.service.IReviewService;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
@org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
public class AdminReviewController {

    private final IReviewService reviewService;

    @GetMapping
    public ResponseEntity<Page<ReviewAdminDTO>> getAllReviews(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        try {
            Page<Review> reviews = reviewService.getAllReviews(productId, userId, page, size);
            Page<ReviewAdminDTO> reviewDTOs = reviews.map(this::convertToDTO);
            return new ResponseEntity<>(reviewDTOs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse> deleteReview(@PathVariable Long reviewId) {
        try {
            reviewService.deleteReviewByAdmin(reviewId);

            ApiResponse response = new ApiResponse();
            response.setMessage("Review deleted successfully");
            response.setStatus(true);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse response = new ApiResponse();
            response.setMessage("Failed to delete review: " + e.getMessage());
            response.setStatus(false);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ReviewAdminDTO convertToDTO(Review review) {
        ReviewAdminDTO dto = new ReviewAdminDTO();
        dto.setId(review.getId());
        dto.setUserId(review.getUser().getId());
        dto.setUserEmail(review.getUser().getEmail());
        dto.setProductId(review.getProduct().getId());
        dto.setProductName(review.getProduct().getProduct_name());
        dto.setReview(review.getReview());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }
}
