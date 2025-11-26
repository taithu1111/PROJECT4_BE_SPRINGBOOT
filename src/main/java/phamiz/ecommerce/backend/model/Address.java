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
    private String streetAddress;

    @Column(name = "city")
    private String city;

    @Column(name = "zip_code")
    private String zipCode;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    @JsonIgnore
    private User user;

}
