package phamiz.ecommerce.backend.dto.Product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import phamiz.ecommerce.backend.model.ProductColor;
import phamiz.ecommerce.backend.model.ProductImage;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @Min(0)
    private int price;

    @Min(0)
    private int quantity;

    @NotBlank
    private String brand;

    private Set<ProductColor> colors;
    private List<ProductImage> images;

    @NotBlank
    private String firstLevelCategory;

    @NotBlank
    private String secondLevelCategory;
}