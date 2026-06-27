package com.example.property_management.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(
        name = "LoginResponse",
        description = "Response returned after a successful user authentication."
)
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
            description = "Authenticated user's phone number.",
            example = "+15551234567",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String phone;

    @Schema(
            description = "JWT access token used to authenticate subsequent API requests.",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String token;
}