package phamiz.ecommerce.backend.dto.Product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRawDTO {
    private Long id;
    private String productName;
    private String description;
    private int quantity;
    private int price;
    private String brand;
    private Set<String> colors;
    private List<String> images;
}
