package phamiz.ecommerce.backend.dto.Review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateReviewRequest {

    @NotBlank(message = "Review text cannot be blank")
    @Size(min = 10, max = 1000, message = "Review must be between 10 and 1000 characters")
    private String review;
}
