package com.example.property_management.dto.assignment;

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
public class CreateAssignmentRequestDTO {

    @NotNull(message = "propertyId is required")
    @Schema(
            description = "ID of the property being assigned.",
            example = "10",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long propertyId;

    @NotNull(message = "userId is required")
    @Schema(
            description = "ID of the user (employee) receiving the property assignment.",
            example = "5",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long userId;

    @NotNull(message = "role is required")
    @Schema(
            description = "Role of the assigned employee.",
            example = "PROPERTY_MANAGER"
    )
    private AssignmentRole role;

    @NotNull(message = "assignedDate is required")
    @Schema(
            description = "Date the property assignment begins.",
            example = "2026-07-02",
            type = "string",
            format = "date",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDate assignedDate;

    @NotNull(message = "endDate is required")
    @Schema(
            description = "Date the property assignment ends.",
            example = "2026-12-31",
            type = "string",
            format = "date",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDate endDate;

    @NotNull(message = "status is required")
    @Schema(
            description = "Current assignment status.",
            example = "ACTIVE"
    )
    private AssignmentStatus status;

    @NotNull(message = "status is isReplaceExisting")
    @Schema(
            description = "if there's an active assignment for this property to another user would you want to end their assignment and assign the property to this user ?.",
            example = "false"
    )
    private boolean isReplaceExisting ;
}