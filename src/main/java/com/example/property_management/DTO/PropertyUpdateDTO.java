package com.example.property_management.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PropertyUpdateDTO {
    private String propertyName;
    private Double propertyValue;
    private String propertyType;
    private String propertyStatus;
    private String ownerEmail;
    private Integer rooms;
    private String location;
}
