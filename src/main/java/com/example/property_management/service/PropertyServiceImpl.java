package com.example.property_management.service;

import com.example.property_management.DTO.PropertyDTO;
import com.example.property_management.entity.PropertyEntity;
import com.example.property_management.repository.PropertyRepository;
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
                        .orElseThrow(() -> new RuntimeException("Property with id " + id + " does not exist"));

        BeanUtils.copyProperties(propertyDTO, existingProperty, "id");
        PropertyEntity updatedProperty = propertyRepository.save(existingProperty);

        PropertyDTO updatedPropertyDTO = new PropertyDTO();
        BeanUtils.copyProperties(updatedProperty, updatedPropertyDTO);

        return updatedPropertyDTO;
    }
}
