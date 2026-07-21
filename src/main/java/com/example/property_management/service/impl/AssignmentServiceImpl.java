package com.example.property_management.service.impl;

import com.example.property_management.authorization.AssignmentAuthorizationService;
import com.example.property_management.dto.PageResponse;
import com.example.property_management.dto.assignment.AssignmentDTO;
import com.example.property_management.dto.assignment.CreateAssignmentRequestDTO;
import com.example.property_management.dto.auditLog.AssignmentAuditSnapshot;
import com.example.property_management.entity.AssignmentEntity;
import com.example.property_management.entity.PropertyEntity;
import com.example.property_management.entity.UserEntity;
import com.example.property_management.enums.AssignmentStatus;
import com.example.property_management.enums.UserRole;
import com.example.property_management.error.exception.*;
import com.example.property_management.mapper.AssignmentMapper;
import com.example.property_management.repository.AssignmentRepository;
import com.example.property_management.repository.PropertyRepository;
import com.example.property_management.repository.UserRepository;
import com.example.property_management.service.AssignmentService;
import com.example.property_management.service.AuditLogService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final AssignmentAuthorizationService assignmentAuthorizationService;
    private final AssignmentMapper assignmentMapper;
    private final AuditLogService auditLogService;

    public AssignmentServiceImpl(AssignmentRepository assignmentRepository, PropertyRepository propertyRepository, UserRepository userRepository, AssignmentAuthorizationService assignmentAuthorizationService, AssignmentMapper assignmentMapper, AuditLogService auditLogService) {
        this.assignmentRepository = assignmentRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
        this.assignmentAuthorizationService = assignmentAuthorizationService;
        this.assignmentMapper = assignmentMapper;
        this.auditLogService = auditLogService;
    }

    private static final Logger log = LoggerFactory.getLogger(AssignmentServiceImpl.class);

    @Transactional
    @Override
    public AssignmentDTO createAssignment(CreateAssignmentRequestDTO dto) {

        assignmentAuthorizationService.canCreateAssignment();

        PropertyEntity property = propertyRepository.findById(dto.getPropertyId())
                .orElseThrow(() -> new PropertyNotFoundException("Property not found"));

        UserEntity user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getRole() != UserRole.AGENT) {throw new ForbiddenException("this user is not an agent");}

        AssignmentEntity currentAssignment =
                assignmentRepository.findByProperty_idAndStatus(
                                dto.getPropertyId(),
                                AssignmentStatus.ACTIVE)
                        .orElse(null);

        if (currentAssignment != null &&
                currentAssignment.getUser().getId().equals(user.getId())) {

            throw new AssignmentAlreadyExistsException("Property is already assigned to this agent.");
        }

        if (currentAssignment != null) {

            if (!dto.isReplaceExisting()){
                log.warn("Property {} is already assigned to user {}",
                        dto.getPropertyId(), currentAssignment.getUser().getId());
                throw new PropertyAlreadyAssignedException("property already assigned");
            }

            currentAssignment.setStatus(AssignmentStatus.COMPLETED);
            currentAssignment.setEndDate(LocalDateTime.now());
        }

        AssignmentEntity assignmentEntity = assignmentMapper.toEntity(dto);
        assignmentEntity.setUser(user);
        assignmentEntity.setProperty(property);

        log.info("Creating assignment for propertyId={} and employeeId={}",
                dto.getPropertyId(), dto.getUserId());

        AssignmentEntity result = assignmentRepository.save(assignmentEntity);

        auditLogService.assignmentLog("Assignment", result.getId().toString(), "Create",
                "Created", AssignmentAuditSnapshot.from(result));

        log.info("Assignment created for propertyId={} and employeeId={}",
                dto.getPropertyId(), dto.getUserId());

        AssignmentDTO response = assignmentMapper.toDTO(result);
        response.setUserId(user.getId());
        response.setPropertyId(property.getId());

        return response;
    }

    @Override
    public AssignmentDTO findById(Long id) {

        log.info("searching for assignment id={}", id);

        AssignmentEntity assignment = assignmentRepository.findById(id).
                orElseThrow(() -> new AssignmentNotFoundException("Assignment not found"));

        assignmentAuthorizationService.canGetAssignment(assignment);

        log.info("Assignment found for id={}", id);

        AssignmentDTO response = assignmentMapper.toDTO(assignment);
        response.setUserId(assignment.getUser().getId());
        response.setPropertyId(assignment.getProperty().getId());

        return response;
    }

    @Override
    public PageResponse<AssignmentDTO> findAll(Pageable pageable) {

        assignmentAuthorizationService.canGetAllAssignments();

        log.info("fetching all assignments");

        Page<AssignmentDTO> result = assignmentRepository.findAll(pageable).map(assignmentMapper::toDTO);

        log.info("{} assignments found", result.getSize());

        return new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast());
    }

    @Transactional
    @Override
    public AssignmentDTO end(Long id) {

        assignmentAuthorizationService.canEndAssignment();

        AssignmentEntity assignment = assignmentRepository.findById(id).
                orElseThrow(() -> new AssignmentNotFoundException("Assignment not found"));

        AssignmentAuditSnapshot before = AssignmentAuditSnapshot.from(assignment);

        if (assignment.getStatus() == AssignmentStatus.INACTIVE) {
            log.warn("Attempt to end inactive assignment id={}", id);
            throw new AssignmentAlreadyInactiveException("Assignment is already inactive");
        }

        log.info("ending the assignment of property {} for user {}",
                assignment.getProperty().getId(), assignment.getUser().getId());//test if the returned id is correct

        assignment.setStatus(AssignmentStatus.INACTIVE);
        assignment.setEndDate(LocalDateTime.now());
        AssignmentEntity saved = assignmentRepository.save(assignment);

        auditLogService.assignmentLog("Assignment", id.toString(), "End",
                before, AssignmentAuditSnapshot.from(saved));

        log.info("Assignment of property {} ended for user {}.",
                assignment.getProperty().getId(), assignment.getUser().getId());

        AssignmentDTO responseDTO = assignmentMapper.toDTO(assignment);
        responseDTO.setUserId(assignment.getUser().getId());
        responseDTO.setPropertyId(assignment.getProperty().getId());

        return responseDTO;
    }
}
