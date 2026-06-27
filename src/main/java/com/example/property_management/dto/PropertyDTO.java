package com.example.property_management.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(
        name = "Property",
        description = "Represents a property within the property management system."
)
public class PropertyDTO {

    @Schema(
            description = "Unique identifier of the property.",
            example = "101",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @NotBlank(message = "property name cannot be empty")
    @Schema(
            description = "Name of the property.",
            example = "Sunset Villa",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String propertyName;

    @NotNull(message = "value cannot be empty")
    @Schema(
            description = "Estimated market value of the property.",
            example = "350000.00",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Double propertyValue;

    @Schema(
            description = "Type of property.",
            example = "Residential",
            allowableValues = {
                    "Residential",
                    "Commercial",
                    "Industrial",
                    "Land"
            }
    )
    private String propertyType;

    @NotBlank(message = "property status cannot be empty")
    @Schema(
            description = "Current status of the property.",
            example = "Available",
            requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {
                    "Available",
                    "Sold",
                    "Rented",
                    "Under Contract"
            }
    )
    private String propertyStatus;

    @Schema(
            description = "Number of rooms in the property.",
            example = "4"
    )
    private Integer rooms;

    @NotBlank(message = "property location cannot be empty")
    @Schema(
            description = "Physical location or address of the property.",
            example = "123 Main Street, New York, NY",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String location;

    @Schema(
            description = "Date and time when the property was created.",
            example = "2026-06-26T14:30:15Z",
            format = "date-time",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String createdDate;

}