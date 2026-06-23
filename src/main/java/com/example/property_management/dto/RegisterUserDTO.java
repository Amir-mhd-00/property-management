package com.example.property_management.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RegisterUserDTO {

    @NotBlank(message = "First name cannot be empty")
    private String firstName;
    @NotBlank(message = "Last name cannot be empty")
    private String lastName;
    @Email(message = "Invalid email format")
    @NotBlank(message = "email cannot be empty")
    private String email;
    private String phone;
    @NotBlank(message = "password cannot be empty")
    private String password;

}
