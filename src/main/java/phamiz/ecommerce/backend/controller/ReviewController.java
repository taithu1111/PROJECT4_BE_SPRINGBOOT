package phamiz.ecommerce.backend.controller;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import phamiz.ecommerce.backend.dto.ApiResponse;
import phamiz.ecommerce.backend.dto.Review.ReviewRequest;
import phamiz.ecommerce.backend.exception.ProductException;
import phamiz.ecommerce.backend.exception.UserException;
import phamiz.ecommerce.backend.model.Review;
import phamiz.ecommerce.backend.model.User;
import phamiz.ecommerce.backend.service.IReviewService;
import phamiz.ecommerce.backend.service.IUserService;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final IReviewService reviewService;
    private final IUserService userService;

    @PostMapping("/create")
    public ResponseEntity<Review> createReview(
            @RequestBody ReviewRequest req,
            @RequestHeader("Authorization") String jwt) throws UserException, ProductException {
        User user = userService.findUserProfileByJwt(jwt);
        Review review = reviewService.createReview(req, user);
        return new ResponseEntity<>(review, HttpStatus.CREATED);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Review>> getProductsReview(
            @PathVariable Long productId) throws UserException, ProductException {
        List<Review> reviews = reviewService.getAllReview(productId);
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<Review> updateReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewRequest req,
            @RequestHeader("Authorization") String jwt) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);
        Review review = reviewService.updateReview(reviewId, req.getReview(), user);
        return new ResponseEntity<>(review, HttpStatus.OK);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse> deleteReview(
            @PathVariable Long reviewId,
            @RequestHeader("Authorization") String jwt) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);
        reviewService.deleteReview(reviewId, user);

        ApiResponse response = new ApiResponse();
        response.setMessage("Review deleted successfully");
        response.setStatus(true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}