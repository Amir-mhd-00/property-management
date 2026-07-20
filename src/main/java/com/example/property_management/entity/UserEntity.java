package com.example.property_management.entity;

import com.example.property_management.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Entity
@Table(name = "User_table")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
    @Column(unique = true)
    private String email;
    private String phone;
    private String password;

    @OneToMany(mappedBy = "user")
    private List<AssignmentEntity> assignments;

    @OneToMany(mappedBy = "owner")
    private List<PropertyEntity> properties;
}
