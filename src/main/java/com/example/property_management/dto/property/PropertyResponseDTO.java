package com.example.property_management.dto.property;

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
    private String propertyType;
    private String propertyStatus;
    private Integer rooms;
    private String location;
    private Long OwnerId;
    private LocalDateTime createdDate;


}