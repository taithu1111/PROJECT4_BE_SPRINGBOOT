package phamiz.ecommerce.backend.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import phamiz.ecommerce.backend.dto.ApiResponse;
import phamiz.ecommerce.backend.dto.Rating.RatingAdminDTO;
import phamiz.ecommerce.backend.model.Rating;
import phamiz.ecommerce.backend.service.IRatingService;

@RestController
@RequestMapping("/api/admin/ratings")
@RequiredArgsConstructor
@org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
public class AdminRatingController {

    private final IRatingService ratingService;

    @GetMapping
    public ResponseEntity<Page<RatingAdminDTO>> getAllRatings(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        try {
            Page<Rating> ratings = ratingService.getAllRatings(productId, userId, page, size);
            Page<RatingAdminDTO> ratingDTOs = ratings.map(this::convertToDTO);
            return new ResponseEntity<>(ratingDTOs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{ratingId}")
    public ResponseEntity<ApiResponse> deleteRating(@PathVariable Long ratingId) {
        try {
            ratingService.deleteRatingByAdmin(ratingId);

            ApiResponse response = new ApiResponse();
            response.setMessage("Rating deleted successfully");
            response.setStatus(true);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse response = new ApiResponse();
            response.setMessage("Failed to delete rating: " + e.getMessage());
            response.setStatus(false);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private RatingAdminDTO convertToDTO(Rating rating) {
        RatingAdminDTO dto = new RatingAdminDTO();
        dto.setId(rating.getId());
        dto.setUserId(rating.getUser().getId());
        dto.setUserEmail(rating.getUser().getEmail());
        dto.setProductId(rating.getProduct().getId());
        dto.setProductName(rating.getProduct().getProduct_name());
        dto.setRating(rating.getRating());
        dto.setCreatedAt(rating.getCreatedAt());
        return dto;
    }
}
