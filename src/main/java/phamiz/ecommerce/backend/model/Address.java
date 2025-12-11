package phamiz.ecommerce.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "street_address")
    @jakarta.validation.constraints.NotBlank(message = "Street address is required")
    private String streetAddress;

    @Column(name = "city")
    @jakarta.validation.constraints.NotBlank(message = "City is required")
    private String city;

    @Column(name = "zip_code")
    @jakarta.validation.constraints.NotBlank(message = "Zip code is required")
    private String zipCode;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    @JsonIgnore
    private User user;

}
