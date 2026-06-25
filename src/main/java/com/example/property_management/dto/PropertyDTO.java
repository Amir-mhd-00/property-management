package com.example.property_management.dto;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PropertyDTO {

    private Long id;
    @Column(unique = true)
    private String propertyName;
    private Double propertyValue;
    private String propertyType;
    private String propertyStatus;
    private Integer rooms;
    private String location;
    private String createdDate;

}
