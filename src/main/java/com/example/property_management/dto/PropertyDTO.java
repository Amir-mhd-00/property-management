package com.example.property_management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PropertyDTO {

    private Long id;
    @NotBlank(message = "property name cannot be empty")
    private String propertyName;
    @NotNull(message = "value cannot be empty")
    private Double propertyValue;
    private String propertyType;
    @NotBlank(message = "property status cannot be empty")
    private String propertyStatus;
    private Integer rooms;
    @NotBlank(message = "property location cannot be empty")
    private String location;
    private String createdDate;

}
