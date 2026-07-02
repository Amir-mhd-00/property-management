package com.example.property_management.service.impl;

import com.example.property_management.dto.AssignmentDTO;
import com.example.property_management.dto.CreateAssignmentRequestDTO;
import com.example.property_management.entity.AssignmentEntity;
import com.example.property_management.entity.PropertyEntity;
import com.example.property_management.entity.UserEntity;
import com.example.property_management.error.exception.PropertyNotFoundException;
import com.example.property_management.error.exception.UserNotFoundException;
import com.example.property_management.repository.AssignmentRepository;
import com.example.property_management.repository.PropertyRepository;
import com.example.property_management.repository.UserRepository;
import com.example.property_management.service.AssignmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    public AssignmentServiceImpl(AssignmentRepository assignmentRepository, PropertyRepository propertyRepository, UserRepository userRepository) {
        this.assignmentRepository = assignmentRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
    }

    private static final Logger log = LoggerFactory.getLogger(AssignmentServiceImpl.class);

    @Override
    public AssignmentDTO createAssignment(CreateAssignmentRequestDTO dto) {

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

        return response;
    }
}
