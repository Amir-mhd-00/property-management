package com.example.property_management.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(
        name = "LoginRequest",
        description = "Request payload used to authenticate a registered user."
)
@Data
public class LoginRequestDTO {

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email cannot be empty")
    @Schema(
            description = "Registered email address of the user.",
            example = "john.doe@example.com",
            format = "email",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Schema(
            description = "Password associated with the user account.",
            example = "MySecurePassword123!",
            requiredMode = Schema.RequiredMode.REQUIRED,
            format = "password"
    )
    private String password;
}