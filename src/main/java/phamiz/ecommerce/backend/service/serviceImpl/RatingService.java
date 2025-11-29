package phamiz.ecommerce.backend.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import phamiz.ecommerce.backend.dto.Rating.RatingRequest;
import phamiz.ecommerce.backend.dto.Rating.UpdateRatingRequest;
import phamiz.ecommerce.backend.exception.ProductException;
import phamiz.ecommerce.backend.model.Product;
import phamiz.ecommerce.backend.model.Rating;
import phamiz.ecommerce.backend.model.User;
import phamiz.ecommerce.backend.repositories.IRatingRepository;
import phamiz.ecommerce.backend.service.IProductService;
import phamiz.ecommerce.backend.service.IRatingService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RatingService implements IRatingService {

    private final IRatingRepository ratingRepository;
    private final IProductService productService;

    @Override
    public Rating createRating(RatingRequest request, User user) throws ProductException {
        Product product = productService.findProductById(request.getProductId());
        Rating rating = new Rating();
        rating.setProduct(product);
        rating.setRating(request.getRating());
        rating.setUser(user);
        rating.setCreatedAt(LocalDateTime.now());

        return ratingRepository.save(rating);
    }

    @Override
    public List<Rating> getProductsRating(Long productId) {
        return ratingRepository.getAllProductsRating(productId);
    }

    @Override
    public org.springframework.data.domain.Page<Rating> getAllRatings(Long productId, Long userId, Integer page,
            Integer size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return ratingRepository.findByFilter(productId, userId, pageable);
    }

    @Override
    public Rating updateRating(Long ratingId, UpdateRatingRequest request, User user) throws Exception {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new Exception("Rating not found with id: " + ratingId));

        // Authorization check: user can only update their own rating
        if (!rating.getUser().getId().equals(user.getId())) {
            throw new Exception("You are not authorized to update this rating");
        }

        rating.setRating(request.getRating());
        rating.setCreatedAt(LocalDateTime.now()); // Update timestamp

        return ratingRepository.save(rating);
    }

    @Override
    public void deleteRating(Long ratingId, User user) throws Exception {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new Exception("Rating not found with id: " + ratingId));

        // Authorization check: user can only delete their own rating
        if (!rating.getUser().getId().equals(user.getId())) {
            throw new Exception("You are not authorized to delete this rating");
        }

        ratingRepository.delete(rating);
    }
}