package phamiz.ecommerce.backend.dto.Cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddItemRequest {
    @jakarta.validation.constraints.NotNull(message = "Product ID is required")
    private Long productId;

    @jakarta.validation.constraints.Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    private Integer price;
}