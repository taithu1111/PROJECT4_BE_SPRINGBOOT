package phamiz.ecommerce.backend.service;

import phamiz.ecommerce.backend.dto.Rating.RatingRequest;
import phamiz.ecommerce.backend.dto.Rating.UpdateRatingRequest;
import phamiz.ecommerce.backend.exception.ProductException;
import phamiz.ecommerce.backend.model.Rating;
import phamiz.ecommerce.backend.model.User;

import java.util.List;

public interface IRatingService {
    public Rating createRating(RatingRequest request, User user) throws ProductException;

    public List<Rating> getProductsRating(Long productId);

    public org.springframework.data.domain.Page<Rating> getAllRatings(Long productId, Long userId, Integer page,
            Integer size);

    public Rating updateRating(Long ratingId, UpdateRatingRequest request, User user) throws Exception;

    public void deleteRating(Long ratingId, User user) throws Exception;

    void deleteRatingByAdmin(Long ratingId) throws Exception;
}
