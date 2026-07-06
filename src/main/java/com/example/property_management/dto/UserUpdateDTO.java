package com.example.property_management.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(
        name = "RegisterUser",
        description = "Request payload used to register a new user account."
)
public class UserUpdateDTO {

    @NotBlank(message = "First name cannot be empty")
    @Schema(
            description = "User's first name.",
            example = "John",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String firstName;

    @NotBlank(message = "Last name cannot be empty")
    @Schema(
            description = "User's last name.",
            example = "Doe",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String lastName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "email cannot be empty")
    @Schema(
            description = "Unique email address used for authentication.",
            example = "john.doe@example.com",
            format = "email",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    @Schema(
            description = "User's contact phone number.",
            example = "+15551234567"
    )
    private String phone;
}
