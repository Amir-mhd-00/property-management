package com.example.property_management.dto.property;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(
        name = "Property",
        description = "Represents a property within the property management system."
)
public class PropertyCreateDTO {

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

    @NotNull(message = "Property value cannot be empty")
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

    @NotNull(message = "Owner id cannot be null")
    @Schema(
            description = "ID of the property owner.",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long ownerId;


    public PropertyCreateDTO(String propertyName, double propertyValue, String propertyStatus, String location) {
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
        this.propertyStatus = propertyStatus;
        this.location = location;
    }
}