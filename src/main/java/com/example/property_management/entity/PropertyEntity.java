package com.example.property_management.entity;

import com.example.property_management.enums.PropertyStatus;
import com.example.property_management.enums.PropertyType;
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

    @Enumerated(EnumType.STRING)
    private PropertyType propertyType;

    @Enumerated(EnumType.STRING)
    private PropertyStatus propertyStatus;
    private Integer rooms;
    private String location;
    private LocalDateTime createdDate;

    @ManyToOne
    @JoinColumn(name = "owner_id",  nullable = false)
    private UserEntity owner;

    @OneToMany(mappedBy = "property")
    private List<AssignmentEntity> assignments;

    public PropertyEntity(String propertyName, double propertyValue, PropertyStatus propertyStatus, String location) {
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
        this.propertyStatus = propertyStatus;
        this.location = location;
    }
}
