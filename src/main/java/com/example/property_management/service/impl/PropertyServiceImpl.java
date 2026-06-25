package com.example.property_management.service.impl;

import com.example.property_management.dto.PropertyDTO;
import com.example.property_management.dto.PropertyUpdateDTO;
import com.example.property_management.entity.PropertyEntity;
import com.example.property_management.error.exception.PropertyAlreadyExistsException;
import com.example.property_management.error.exception.PropertyNotFoundException;
import com.example.property_management.repository.PropertyRepository;
import com.example.property_management.service.PropertyService;
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

    @Override
    public PropertyDTO getProperty(Long id) {

        PropertyEntity property = getPropertyOrThrow(id);

        PropertyDTO responseDTO = new PropertyDTO();
        BeanUtils.copyProperties(property, responseDTO);

        return responseDTO;
    }

    @Override
    public PropertyDTO createProperty(PropertyDTO propertyDTO){

        if (propertyRepository.findByPropertyName(propertyDTO.getPropertyName()).isPresent()) {
            throw  new PropertyAlreadyExistsException(
                    String.format("Property with name %s already exists", propertyDTO.getPropertyName()));
        }

        PropertyEntity entity = new PropertyEntity();
        BeanUtils.copyProperties(propertyDTO, entity);
        PropertyEntity responseEntity = propertyRepository.save(entity);
        PropertyDTO responseDTO = new PropertyDTO();
        BeanUtils.copyProperties(responseEntity, responseDTO);

        return responseDTO;
    }

    @Override
    public List<PropertyDTO> getAllProperties(){

        List<PropertyEntity> properties = propertyRepository.findAll();

        List<PropertyDTO> propertiesDTO = new ArrayList<>();

        for (PropertyEntity propertyEntity : properties){
            PropertyDTO propertyDTO = new PropertyDTO();
            BeanUtils.copyProperties(propertyEntity, propertyDTO);
            propertiesDTO.add(propertyDTO);
        }

        return propertiesDTO;
    }

    @Override
    public PropertyDTO updateProperty(Long id, PropertyDTO propertyDTO) {
        PropertyEntity existingProperty = getPropertyOrThrow(id);

        BeanUtils.copyProperties(propertyDTO, existingProperty, "id");

        PropertyEntity updatedProperty = propertyRepository.save(existingProperty);

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

        PropertyEntity savedProperty = propertyRepository.save(property);

        PropertyDTO response = new PropertyDTO();
        BeanUtils.copyProperties(savedProperty, response);

        return response;
    }

    @Override
    public void deleteProperty(Long id) {

        PropertyEntity property = getPropertyOrThrow(id);

        propertyRepository.delete(property);

    }

    private PropertyEntity getPropertyOrThrow(Long id){

        return propertyRepository.findById(id).
                orElseThrow(() -> new PropertyNotFoundException(
                        String.format("Property with id %d not found", id)));
    }
}
