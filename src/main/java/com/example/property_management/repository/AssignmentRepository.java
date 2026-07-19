package com.example.property_management.repository;

import com.example.property_management.entity.AssignmentEntity;
import com.example.property_management.enums.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface AssignmentRepository extends JpaRepository<AssignmentEntity, Long> {
    List<AssignmentEntity> findAllByUser_id(Long id);
    List<AssignmentEntity> findAllByProperty_id(Long id);
    Optional<AssignmentEntity> findByProperty_idAndStatus(Long id, AssignmentStatus status);
    boolean existsByUserIdAndPropertyId(Long userId, Long propertyId);
}
