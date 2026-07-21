package com.example.property_management.dto.property;

import com.example.property_management.enums.PropertyStatus;
import com.example.property_management.enums.PropertyType;
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
public class PropertyUpdateDTO {

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

    @NotNull(message = "property type cannot be empty")
    @Schema(
            description = "Type of property.",
            example = "APARTMENT",
            allowableValues = {
                    "HOUSE",
                    "APARTMENT",
                    "VILLA",
                    "CONDO",
                    "TOWNHOUSE",
                    "OFFICE",
                    "COMMERCIAL",
                    "WAREHOUSE",
                    "LAND"
            }
    )
    private PropertyType propertyType;

    @NotNull(message = "property status cannot be empty")
    @Schema(
            description = "Current status of the property.",
            example = "AVAILABLE",
            requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {
                    "AVAILABLE",
                    "OCCUPIED",
                    "UNDER_MAINTENANCE",
                    "RESERVED",
                    "SOLD",
                    "INACTIVE"
            }
    )
    private PropertyStatus propertyStatus;

    @NotNull
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

}

