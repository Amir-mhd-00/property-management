package com.example.property_management.service.impl;

import com.example.property_management.dto.PropertyDTO;
import com.example.property_management.dto.PropertyUpdateDTO;
import com.example.property_management.entity.PropertyEntity;
import com.example.property_management.error.exception.PropertyAlreadyExistsException;
import com.example.property_management.error.exception.PropertyNotFoundException;
import com.example.property_management.repository.PropertyRepository;
import com.example.property_management.service.PropertyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;


@Service
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;

    public PropertyServiceImpl(PropertyRepository propertyRepository) {

        this.propertyRepository = propertyRepository;
    }

    private static final Logger logger = LoggerFactory.getLogger(PropertyServiceImpl.class);

    @Override
    public PropertyDTO getProperty(Long id) {

        logger.debug("Fetching property id={}", id);

        PropertyEntity property = getPropertyOrThrow(id);

        logger.debug("property id={} fetched successfully", id);

        PropertyDTO responseDTO = new PropertyDTO();
        BeanUtils.copyProperties(property, responseDTO);

        return responseDTO;
    }

    @Override
    public PropertyDTO createProperty(PropertyDTO propertyDTO){

        if (propertyRepository.findByPropertyName(propertyDTO.getPropertyName()).isPresent()) {

            logger.warn("property creation failed property {} already exists", propertyDTO.getPropertyName());

            throw  new PropertyAlreadyExistsException(
                    String.format("Property with name %s already exists", propertyDTO.getPropertyName()));
        }

        PropertyEntity entity = new PropertyEntity();
        BeanUtils.copyProperties(propertyDTO, entity);

        logger.info("creating property {}", propertyDTO.getPropertyName());

        PropertyEntity responseEntity = propertyRepository.save(entity);

        logger.info(
                "Property '{}' created successfully. id={}",
                entity.getPropertyName(),
                entity.getId()
        );

        PropertyDTO responseDTO = new PropertyDTO();
        BeanUtils.copyProperties(responseEntity, responseDTO);

        return responseDTO;
    }

    @Override
    public List<PropertyDTO> getAllProperties(){

        logger.info("fetching all properties");

        List<PropertyEntity> properties = propertyRepository.findAll();

        logger.info("fetched {} properties", properties.size());

        List<PropertyDTO> propertiesDTO = new ArrayList<>();

        for (PropertyEntity propertyEntity : properties){
            PropertyDTO propertyDTO = new PropertyDTO();
            BeanUtils.copyProperties(propertyEntity, propertyDTO);
            propertiesDTO.add(propertyDTO);
        }

        return propertiesDTO;
    }

    @Override
    public PropertyDTO updateProperty(Long id, PropertyDTO dto) {
        PropertyEntity existingProperty = getPropertyOrThrow(id);

        BeanUtils.copyProperties(dto, existingProperty, "id");
        
        logger.info("PUT updating property id={}", id);

        if (propertyRepository.findByPropertyName(dto.getPropertyName()).isPresent()) {

            logger.warn("Updating failed property {} already exists", dto.getPropertyName());

            throw  new PropertyAlreadyExistsException(
                    String.format("Property with name %s already exists", dto.getPropertyName()));
        }

        PropertyEntity updatedProperty = propertyRepository.save(existingProperty);

        logger.info("PUT Property updated successfully. id={}", id);

        PropertyDTO updatedPropertyDTO = new PropertyDTO();
        BeanUtils.copyProperties(updatedProperty, updatedPropertyDTO);

        return updatedPropertyDTO;
    }

    @Override
    public PropertyDTO partialUpdateProperty(Long id, PropertyUpdateDTO dto) {
        PropertyEntity property = getPropertyOrThrow(id);

        if (dto.getPropertyName() != null){
            property.setPropertyName(dto.getPropertyName());
        }
        if (dto.getPropertyValue() != null){
            property.setPropertyValue(dto.getPropertyValue());
        }
        if (dto.getPropertyType() != null){
            property.setPropertyType(dto.getPropertyType());
        }
        if  (dto.getPropertyStatus() != null){
            property.setPropertyStatus(dto.getPropertyStatus());
        }
        if (dto.getRooms() != null){
            property.setRooms(dto.getRooms());
        }
        if (dto.getLocation() != null){
            property.setLocation(dto.getLocation());
        }

        logger.info("PATCH Updating property id={}", id);

        PropertyEntity savedProperty = propertyRepository.save(property);

        logger.info("PATCH Property updated successfully. id={}", id);

        PropertyDTO response = new PropertyDTO();
        BeanUtils.copyProperties(savedProperty, response);

        return response;
    }

    @Override
    public void deleteProperty(Long id) {

        logger.info("deleting property id={}", id);

        PropertyEntity property = getPropertyOrThrow(id);

        propertyRepository.delete(property);

        logger.info("Property deleted successfully. id={}", id);

    }


    private PropertyEntity getPropertyOrThrow(Long id){

        return propertyRepository.findById(id).
                orElseThrow(() -> new PropertyNotFoundException(
                        String.format("Property with id %d not found", id)));
    }
}
