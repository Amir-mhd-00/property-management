package com.example.property_management.entity;

import com.example.property_management.enums.AssignmentRole;
import com.example.property_management.enums.AssignmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "Property_assignment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "property_id")
    private PropertyEntity property;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    private AssignmentRole role;

    private LocalDate assignedDate;

    private LocalDate endDate;

    private AssignmentStatus status;
}