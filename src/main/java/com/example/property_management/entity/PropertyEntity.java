package com.example.property_management.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Properties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PropertyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String propertyName;
    private Double propertyValue;
    private String propertyType;
    private String propertyStatus;
    private Integer rooms;
    private String location;
    private LocalDateTime createdDate;

    @ManyToOne
    @JoinColumn(name = "owner_id",  nullable = false)
    private UserEntity owner;

    @OneToMany(mappedBy = "property")
    private List<AssignmentEntity> assignments;

    public PropertyEntity(String propertyName, double propertyValue, String propertyStatus, String location) {
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
        this.propertyStatus = propertyStatus;
        this.location = location;
    }
}
