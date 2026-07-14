package com.example.property_management.dto.property;

import com.example.property_management.enums.PropertyStatus;
import com.example.property_management.enums.PropertyType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PropertyResponseDTO {

    private Long id;
    private String propertyName;
    private Double propertyValue;
    private PropertyType propertyType;
    private PropertyStatus propertyStatus;
    private Integer rooms;
    private String location;
    private Long OwnerId;
    private LocalDateTime createdDate;


}