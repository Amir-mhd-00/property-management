package com.example.property_management.dto;

import com.example.property_management.enums.AssignmentRole;
import com.example.property_management.enums.AssignmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(
        name = "Create Assignment Request",
        description = "Request payload used to assign a property to a user."
)
public class AssignmentDTO {

    private Long id;
    private Long propertyId;
    private Long userId;
    private AssignmentRole role;
    private LocalDate assignedDate;
    private LocalDate endDate;
    private AssignmentStatus status;
}