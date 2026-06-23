package com.example.property_management.service.Impl;

import com.example.property_management.DTO.PropertyDTO;
import com.example.property_management.DTO.PropertyUpdateDTO;
import com.example.property_management.entity.PropertyEntity;
import com.example.property_management.repository.PropertyRepository;
import com.example.property_management.service.PropertyService;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;

    public PropertyServiceImpl(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    @Override
    public PropertyDTO createProperty(PropertyDTO propertyDTO){

        PropertyEntity entity = new PropertyEntity();
        BeanUtils.copyProperties(propertyDTO, entity);
        PropertyEntity responseEntity = propertyRepository.save(entity);
        PropertyDTO responseDTO = new PropertyDTO();
        BeanUtils.copyProperties(responseEntity, responseDTO);

        return responseDTO;
    }

    @Override
    public List<PropertyDTO> getAllProperties(){
        List<PropertyEntity> properties = (List<PropertyEntity>) propertyRepository.findAll();
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
        PropertyEntity existingProperty = propertyRepository.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Property with id " + id + " does not exist"
                        ));

        BeanUtils.copyProperties(propertyDTO, existingProperty, "id");
        PropertyEntity updatedProperty = propertyRepository.save(existingProperty);

        PropertyDTO updatedPropertyDTO = new PropertyDTO();
        BeanUtils.copyProperties(updatedProperty, updatedPropertyDTO);

        return updatedPropertyDTO;
    }

    @Override
    public PropertyDTO updateProperty(Long id, PropertyUpdateDTO dto) {
        PropertyEntity property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Property with id " + id + " does not exist"
                ));

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

        propertyRepository.save(property);
        PropertyDTO updatedPropertyDTO = new PropertyDTO();
        BeanUtils.copyProperties(property, updatedPropertyDTO);

        return updatedPropertyDTO;
    }

    @Override
    public void deleteProperty(Long id) {

        PropertyEntity property = propertyRepository.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Property with id " + id + " does not exist"
                        ));

        propertyRepository.delete(property);

    }
}
