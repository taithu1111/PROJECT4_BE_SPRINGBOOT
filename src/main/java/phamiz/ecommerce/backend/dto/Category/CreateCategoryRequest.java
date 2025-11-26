package phamiz.ecommerce.backend.dto.Category;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategoryRequest {
    private String name;
    private Long parentId; // optional, can be null for top-level categories
    private int level;
}
