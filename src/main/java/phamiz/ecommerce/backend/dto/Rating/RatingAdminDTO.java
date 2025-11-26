package phamiz.ecommerce.backend.dto.Rating;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingAdminDTO {
    private Long id;
    private Long userId;
    private String userEmail;
    private Long productId;
    private String productName;
    private double rating;
    private LocalDateTime createdAt;
}
