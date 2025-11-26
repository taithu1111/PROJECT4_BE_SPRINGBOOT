package phamiz.ecommerce.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity

public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    private Product product;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    @JsonIgnore
    private User user;
    private String review;
    private LocalDateTime createdAt;
}
