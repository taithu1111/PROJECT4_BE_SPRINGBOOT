package phamiz.ecommerce.backend.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
