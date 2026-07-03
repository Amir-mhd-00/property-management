package com.example.property_management.service.impl;

import com.example.property_management.dto.AssignmentDTO;
import com.example.property_management.dto.CreateAssignmentRequestDTO;
import com.example.property_management.entity.AssignmentEntity;
import com.example.property_management.entity.PropertyEntity;
import com.example.property_management.entity.UserEntity;
import com.example.property_management.enums.AssignmentStatus;
import com.example.property_management.error.exception.*;
import com.example.property_management.repository.AssignmentRepository;
import com.example.property_management.repository.PropertyRepository;
import com.example.property_management.repository.UserRepository;
import com.example.property_management.service.AssignmentService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AssignmentServiceImpl implements AssignmentService {

    //making the log messages show unique username and property name

    private final AssignmentRepository assignmentRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    public AssignmentServiceImpl(AssignmentRepository assignmentRepository, PropertyRepository propertyRepository, UserRepository userRepository) {
        this.assignmentRepository = assignmentRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
    }

    private static final Logger log = LoggerFactory.getLogger(AssignmentServiceImpl.class);

    @Transactional
    @Override
    public AssignmentDTO createAssignment(CreateAssignmentRequestDTO dto) {

        // property {id} is already assigned to {id} would u like to end their assignment and assign it to {id}?

        propertyRepository.findById(dto.getPropertyId())
                .orElseThrow(() -> new PropertyNotFoundException("Property not found"));

        userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        boolean isAssigned = assignmentRepository.existsByProperty_idAndStatus(
                dto.getPropertyId(),
                AssignmentStatus.ACTIVE
        );

        if (isAssigned) {
            log.warn("Property {} is already assigned to user {}", dto.getPropertyId(), dto.getUserId());
            throw new PropertyAlreadyAssignedException("property already assigned");
        }

        AssignmentEntity assignmentEntity = new AssignmentEntity();
        BeanUtils.copyProperties(dto, assignmentEntity);

        PropertyEntity property = propertyRepository.findById(dto.getPropertyId()).
                orElseThrow(() -> new PropertyNotFoundException("Property not found"));
        UserEntity user =  userRepository.findById(dto.getUserId()).
                orElseThrow(() -> new UserNotFoundException("User not found"));

        assignmentEntity.setUser(user);
        assignmentEntity.setProperty(property);

        log.info("Creating assignment for propertyId={} and employeeId={}",
                dto.getPropertyId(), dto.getUserId());

        AssignmentEntity result = assignmentRepository.save(assignmentEntity);

        log.info("Assignment created for propertyId={} and employeeId={}",
                dto.getPropertyId(), dto.getUserId());

        AssignmentDTO response = new AssignmentDTO();
        BeanUtils.copyProperties(result, response);
        response.setUserId(user.getId());
        response.setPropertyId(property.getId());

        return response;
    }

    @Override
    public AssignmentDTO findById(Long id) {

        log.info("searching for assignment id={}", id);

        AssignmentEntity assignment = assignmentRepository.findById(id).
                orElseThrow(() -> new AssignmentNotFoundException("Assignment not found"));

        log.info("Assignment found for id={}", id);

        AssignmentDTO response = new AssignmentDTO();
        BeanUtils.copyProperties(assignment, response);
        response.setUserId(assignment.getUser().getId());
        response.setPropertyId(assignment.getProperty().getId());

        return response;
    }

    @Override
    public List<AssignmentDTO> findAll() {

        log.info("fetching all assignments");

        List<AssignmentEntity> result = assignmentRepository.findAll();

        log.info("{} assignments found", result.size());

        List<AssignmentDTO> response = new ArrayList<>();
        for (AssignmentEntity assignment : result) {
            AssignmentDTO responseDTO = new AssignmentDTO();
            BeanUtils.copyProperties(assignment, responseDTO);
            responseDTO.setUserId(assignment.getUser().getId());
            responseDTO.setPropertyId(assignment.getProperty().getId());
            response.add(responseDTO);
        }

        return response;
    }

    @Transactional
    @Override
    public AssignmentDTO end(Long id) {

        AssignmentEntity assignment = assignmentRepository.findById(id).
                orElseThrow(() -> new AssignmentNotFoundException("Assignment not found"));

        if (assignment.getStatus() == AssignmentStatus.INACTIVE) {
            log.warn("Attempt to end inactive assignment id={}", id);
            throw new AssignmentAlreadyInactiveException("Assignment is already inactive");
        }

        log.info("ending the assignment of property {} for user {}",
                assignment.getProperty().getId(), assignment.getUser().getId());//test if the returned id is correct

        assignment.setStatus(AssignmentStatus.INACTIVE);
        assignmentRepository.save(assignment);

        log.info("Assignment of property {} ended for user {}.",
                assignment.getProperty().getId(), assignment.getUser().getId());

        AssignmentDTO responseDTO = new AssignmentDTO();
        BeanUtils.copyProperties(assignment, responseDTO);
        responseDTO.setUserId(assignment.getUser().getId());
        responseDTO.setPropertyId(assignment.getProperty().getId());

        return responseDTO;
    }
}
