package com.example.property_management.dto.property;

import com.example.property_management.enums.PropertyStatus;
import com.example.property_management.enums.PropertyType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Schema(
        name = "PropertyUpdate",
        description = "Request payload used to update an existing property. Only the provided fields will be updated."
)
public class PropertyPatchDTO {

    @Schema(
            description = "Updated name of the property.",
            example = "Sunset Villa"
    )
    private String propertyName;

    @Schema(
            description = "Updated market value of the property.",
            example = "425000.00"
    )
    private Double propertyValue;

    @Schema(
            description = "Updated type of the property.",
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

    @Schema(
            description = "Updated status of the property.",
            example = "AVAILABLE",
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

    @Schema(
            description = "Updated number of rooms.",
            example = "5"
    )
    private Integer rooms;

    @Schema(
            description = "Updated property location or address.",
            example = "456 Oak Avenue, Los Angeles, CA"
    )
    private String location;
}