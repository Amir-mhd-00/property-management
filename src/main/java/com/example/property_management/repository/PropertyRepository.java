package com.example.property_management.repository;

import com.example.property_management.entity.PropertyEntity;
import com.example.property_management.enums.PropertyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyRepository extends JpaRepository<PropertyEntity, Long> {

    Optional<PropertyEntity> findByPropertyName(String propertyName);
    List<PropertyEntity> findAllByPropertyStatus(PropertyStatus propertyStatus);
    boolean existsByIdAndOwnerId(Long propertyId, Long ownerId);

}
