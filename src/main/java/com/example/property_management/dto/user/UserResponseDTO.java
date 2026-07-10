package com.example.property_management.dto.user;

import com.example.property_management.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(
        name = "UserResponse",
        description = "Response containing the details of a registered user."
)
public class UserResponseDTO {

    @Schema(
            description = "Unique identifier of the user.",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @Schema(
            description = "User's first name.",
            example = "John",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String firstName;

    @Schema(
            description = "User's last name.",
            example = "Doe",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String lastName;

    @Schema(
            description = "User's email address.",
            example = "john.doe@example.com",
            format = "email",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String email;

    @Schema(
            description = "User's contact phone number.",
            example = "+15551234567",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String phone;
    @Schema(
            description = "User's role.",
            example = "GUEST",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private UserRole role;
}