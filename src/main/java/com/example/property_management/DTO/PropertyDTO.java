package com.example.property_management.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PropertyDTO {

    private Long id;
    private String propertyName;
    private Double propertyValue;
    private String propertyType;
    private String propertyStatus;
    private Integer rooms;
    private String location;
    private String createdDate;

}
