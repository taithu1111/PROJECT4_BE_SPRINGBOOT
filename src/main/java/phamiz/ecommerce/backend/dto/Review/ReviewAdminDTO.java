package phamiz.ecommerce.backend.dto.Review;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewAdminDTO {
    private Long id;
    private Long userId;
    private String userEmail;
    private Long productId;
    private String productName;
    private String review;
    private LocalDateTime createdAt;
}
