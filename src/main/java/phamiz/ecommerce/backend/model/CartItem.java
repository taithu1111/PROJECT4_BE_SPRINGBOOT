package phamiz.ecommerce.backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO )
    private Long id;

    @ManyToOne
    @JsonBackReference // CartItem là con của Cart Em phúc thêm để sửa StackOverFlow
    private Cart cart;

    @ManyToOne
    private Product product;

    private int quantity;
    private Integer price;

}
