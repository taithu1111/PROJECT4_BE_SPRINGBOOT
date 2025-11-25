package phamiz.ecommerce.backend.model;

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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    private Cart cart;

    @ManyToOne
    private Product product;

    private int quantity;
    private Integer price;

}
