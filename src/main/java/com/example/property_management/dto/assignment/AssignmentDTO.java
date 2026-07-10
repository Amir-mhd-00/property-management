package com.example.property_management.dto.assignment;

import com.example.property_management.enums.AssignmentRole;
import com.example.property_management.enums.AssignmentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentDTO {

    private Long id;
    private Long propertyId;
    private Long userId;
    private AssignmentRole role;
    private LocalDate assignedDate;
    private LocalDate endDate;
    private AssignmentStatus status;
}