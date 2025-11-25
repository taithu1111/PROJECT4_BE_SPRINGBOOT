package phamiz.ecommerce.backend.dto.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for User responses - excludes sensitive fields like password
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String mobile;
    private String role;
    private Boolean active;
    private LocalDateTime createdAt;

    // SECURITY: Password field is intentionally excluded
    // SECURITY: Address/Rating/Review relationships excluded to prevent circular
    // references
}
