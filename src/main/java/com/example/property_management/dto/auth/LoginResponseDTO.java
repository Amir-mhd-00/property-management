package com.example.property_management.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Schema(
        name = "LoginResponse",
        description = "Response returned after a successful user authentication."
)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDTO {

    @Schema(
            description = "Unique identifier of the authenticated user.",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @Schema(
            description = "Authenticated user's first name.",
            example = "John",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String firstName;

    @Schema(
            description = "Authenticated user's last name.",
            example = "Doe",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String lastName;

    @Schema(
            description = "Authenticated user's email address.",
            example = "john.doe@example.com",
            format = "email",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String email;

    @Schema(
            description = "users role.",
            example = "AGENT",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String role;

}