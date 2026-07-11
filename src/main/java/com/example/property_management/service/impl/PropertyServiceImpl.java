package com.example.property_management.service.impl;

import com.example.property_management.authorization.PropertyAuthorizationService;
import com.example.property_management.dto.assignment.AssignmentDTO;
import com.example.property_management.dto.PageResponse;
import com.example.property_management.dto.property.PropertyCreateDTO;
import com.example.property_management.dto.property.PropertyResponseDTO;
import com.example.property_management.dto.property.PropertyUpdateDTO;
import com.example.property_management.entity.AssignmentEntity;
import com.example.property_management.entity.PropertyEntity;
import com.example.property_management.entity.UserEntity;
import com.example.property_management.enums.UserRole;
import com.example.property_management.error.exception.ForbiddenException;
import com.example.property_management.error.exception.PropertyAlreadyExistsException;
import com.example.property_management.error.exception.PropertyNotFoundException;
import com.example.property_management.error.exception.UserNotFoundException;
import com.example.property_management.mapper.AssignmentMapper;
import com.example.property_management.mapper.PropertyMapper;
import com.example.property_management.repository.AssignmentRepository;
import com.example.property_management.repository.PropertyRepository;
import com.example.property_management.repository.UserRepository;
import com.example.property_management.service.PropertyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;


@Service
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;
    private final AssignmentRepository assignmentRepository;
    private final PropertyMapper propertyMapper;
    private final AssignmentMapper assignmentMapper;
    private final PropertyAuthorizationService propertyAuthorizationService;
    private final UserRepository userRepository;

    public PropertyServiceImpl(PropertyRepository propertyRepository,
                               AssignmentRepository assignmentRepository,
                               PropertyMapper propertyMapper, AssignmentMapper assignmentMapper,
                               PropertyAuthorizationService propertyAuthorizationService, UserRepository userRepository) {

        this.propertyRepository = propertyRepository;
        this.assignmentRepository = assignmentRepository;
        this.propertyMapper = propertyMapper;
        this.assignmentMapper = assignmentMapper;
        this.propertyAuthorizationService = propertyAuthorizationService;
        this.userRepository = userRepository;
    }

    private static final Logger logger = LoggerFactory.getLogger(PropertyServiceImpl.class);

    @Override
    public PropertyResponseDTO getProperty(Long id) {

        logger.debug("Fetching property id={}", id);

        PropertyEntity property = getPropertyOrThrow(id);

        logger.debug("property id={} fetched successfully", id);

        return propertyMapper.toDTO(property);
    }


    @Override
    public PropertyResponseDTO createProperty(PropertyCreateDTO property){

        propertyAuthorizationService.canCreateProperty();

        if (propertyRepository.findByPropertyName(property.getPropertyName()).isPresent()) {

            logger.warn("property creation failed property {} already exists", property.getPropertyName());

            throw  new PropertyAlreadyExistsException(
                    String.format("Property with name %s already exists", property.getPropertyName()));
        }

        UserEntity userEntity = userRepository.findById(property.getOwnerId()).
                orElseThrow(() -> new UserNotFoundException("user not found"));

        if (userEntity.getRole() != UserRole.OWNER) {throw new ForbiddenException("user is not an owner");}

        PropertyEntity propertyEntity = propertyMapper.toEntity(property);

        logger.info("creating property {}", property.getPropertyName());

        propertyEntity.setOwner(userEntity);
        propertyEntity.setCreatedDate(LocalDateTime.now(ZoneId.of("UTC")));
        PropertyEntity responseEntity = propertyRepository.save(propertyEntity);

        logger.info(
                "Property '{}' created successfully. id={}",
                propertyEntity.getPropertyName(),
                propertyEntity.getId()
        );

        PropertyResponseDTO responseDTO = propertyMapper.toDTO(responseEntity);
        responseDTO.setOwnerId(userEntity.getId());

        return responseDTO;
    }

    public PageResponse<PropertyResponseDTO> getProperties(Pageable pageable) {

        logger.info("fetching properties");

        Page<PropertyResponseDTO> properties = propertyRepository.
                findAll(pageable).map(propertyMapper::toDTO);

        return new PageResponse<>(
                properties.getContent(),
                properties.getNumber(),
                properties.getSize(),
                properties.getTotalElements(),
                properties.getTotalPages(),
                properties.isFirst(),
                properties.isLast());
    }

    @Override
    public PropertyResponseDTO updateProperty(Long id, PropertyCreateDTO dto) {
        //fix this shit
        propertyAuthorizationService.canUpdateProperty(id);

        PropertyEntity existingProperty = getPropertyOrThrow(id);

        logger.info("'PUT' updating property id={}", id);

        if (propertyRepository.findByPropertyName(dto.getPropertyName())
                .filter(existing -> !existing.getId().equals(id))
                .isPresent()) {

            logger.warn("'PUT' Updating failed property with name {} already exists", dto.getPropertyName());

            throw  new PropertyAlreadyExistsException(
                    String.format("Property with name %s already exists", dto.getPropertyName()));
        }

        UserEntity owner = userRepository.findById(dto.getOwnerId()).
                orElseThrow(() -> new UserNotFoundException("user not found"));

        if (owner.getRole() != UserRole.OWNER) {throw new ForbiddenException("user is not an owner");}

        PropertyEntity updatedProperty =  propertyMapper.toEntity(dto);

        updatedProperty.setId(existingProperty.getId());
        updatedProperty.setCreatedDate(existingProperty.getCreatedDate());

        propertyRepository.save(updatedProperty);

        logger.info("'PUT' Property updated successfully. id={}", id);

        return propertyMapper.toDTO(updatedProperty);
    }

    @Override
    public PropertyResponseDTO partialUpdateProperty(Long id, PropertyUpdateDTO dto) {

        propertyAuthorizationService.canUpdateProperty(id);

        PropertyEntity property = getPropertyOrThrow(id);

        logger.info("'PATCH' Updating property id={}", id);

        if (dto.getPropertyName() != null &&
                propertyRepository.findByPropertyName(dto.getPropertyName())
                .filter(existing -> !existing.getId().equals(id))
                .isPresent()) {

            logger.warn("'PATCH' Updating failed property {} already exists", dto.getPropertyName());

            throw  new PropertyAlreadyExistsException(
                    String.format("Property with name %s already exists", dto.getPropertyName()));
        }

        propertyMapper.updateProperty(dto, property);

        PropertyEntity savedProperty = propertyRepository.save(property);

        logger.info("'PATCH' Property updated successfully. id={}", id);

        PropertyResponseDTO response = propertyMapper.toDTO(savedProperty);
        response.setOwnerId(property.getOwner().getId());

        return response;
    }

    @Override
    public void deleteProperty(Long id) {

        logger.info("deleting property id={}", id);

        PropertyEntity property = getPropertyOrThrow(id);

        propertyAuthorizationService.canDeleteProperty();

        propertyRepository.delete(property);

        logger.info("Property deleted successfully. id={}", id);
    }

    @Override
    public List<PropertyResponseDTO> getAllPropertiesByStatus(String propertyStatus) {

        logger.info("fetching all properties with status : {}", propertyStatus);

        List<PropertyEntity> properties = propertyRepository.findAllByPropertyStatus(propertyStatus);

        logger.info("fetched {} properties with status : {}", properties.size(),  propertyStatus);

        List<PropertyResponseDTO> propertiesDTO = new ArrayList<>();

        for (PropertyEntity propertyEntity : properties) {
            PropertyResponseDTO propertyDTO = propertyMapper.toDTO(propertyEntity);
            propertiesDTO.add(propertyDTO);
        }

        return propertiesDTO;
    }

    @Override
    public List<AssignmentDTO> getAssignmentsByProperty(Long id) {

        propertyRepository.findById(id).orElseThrow(() ->
                new PropertyNotFoundException("Property not found"));

        propertyAuthorizationService.canGetAssignmentsByProperty();

        logger.info("Fetching assignments for propertyId={}", id);

        List<AssignmentEntity> assignmentEntities = assignmentRepository.findAllByProperty_id(id);

        logger.info("{} assignments found for property {}", assignmentEntities.size(), id);

        List<AssignmentDTO> response = new ArrayList<>();
        for (AssignmentEntity assignment : assignmentEntities) {
            AssignmentDTO responseDTO = assignmentMapper.toDTO(assignment);
            responseDTO.setUserId(assignment.getUser().getId());
            responseDTO.setPropertyId(assignment.getProperty().getId());
            response.add(responseDTO);
        }

        return response;
    }

    private PropertyEntity getPropertyOrThrow(Long id){

        return propertyRepository.findById(id).
                orElseThrow(() -> new PropertyNotFoundException(
                        String.format("Property with id %d not found", id)));
    }
}
