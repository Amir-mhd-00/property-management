package com.example.property_management.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(
        name = "PropertyUpdate",
        description = "Request payload used to update an existing property. Only the provided fields will be updated."
)
public class PropertyUpdateDTO {

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
            example = "Residential",
            allowableValues = {
                    "Residential",
                    "Commercial",
                    "Industrial",
                    "Land"
            }
    )
    private String propertyType;

    @Schema(
            description = "Updated status of the property.",
            example = "Available",
            allowableValues = {
                    "Available",
                    "Sold",
                    "Rented",
                    "Under Contract"
            }
    )
    private String propertyStatus;

    @Schema(
            description = "Email address of the property's owner.",
            example = "owner@example.com",
            format = "email"
    )
    private String ownerEmail;

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