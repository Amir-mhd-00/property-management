package com.example.property_management.service.Impl;

import com.example.property_management.dto.PropertyDTO;
import com.example.property_management.dto.PropertyUpdateDTO;
import com.example.property_management.entity.PropertyEntity;
import com.example.property_management.error.exception.PropertyAlreadyExistsException;
import com.example.property_management.error.exception.PropertyNotFoundException;
import com.example.property_management.repository.PropertyRepository;
import com.example.property_management.service.impl.PropertyServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Optional;
import static java.util.Optional.empty;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropertyServiceImplTest {

    @Mock
    private PropertyRepository propertyRepository;
    @InjectMocks
    private PropertyServiceImpl propertyService;

    @Test
    void getPropertySuccessTest() {

        PropertyEntity entity = new PropertyEntity();
        entity.setId(1L);
        entity.setLocation("123 Main St");
        entity.setPropertyStatus("AVAILABLE");

        when(propertyRepository.findById(1L)).
                thenReturn(Optional.of(entity));

        PropertyDTO result = propertyService.getProperty(1L);

        assertEquals(1L, result.getId());
        assertEquals("123 Main St", result.getLocation());
        assertEquals("AVAILABLE", result.getPropertyStatus());

        verify(propertyRepository).findById(1L);

    }

    @Test
    void getPropertyFailureTest(){

        when(propertyRepository.findById(1L)).
                thenReturn(empty());

        assertThrows(PropertyNotFoundException.class,
                () -> propertyService.getProperty(1L));

        verify(propertyRepository).findById(1L);
    }

    @Test
    void createPropertySuccessTest() {

        PropertyDTO propertyDTO = new PropertyDTO(
                "test name",
                124400000.0,
                "AVAILABLE",
                "123 Main St"
        );

        PropertyEntity  propertyEntity = new PropertyEntity();
        propertyEntity.setId(1L);
        BeanUtils.copyProperties(propertyDTO, propertyEntity);

        when(propertyRepository.findByPropertyName(propertyDTO.getPropertyName())).
                thenReturn(empty());
        when(propertyRepository.save(any(PropertyEntity.class))).
                thenReturn(propertyEntity);

        PropertyDTO result = propertyService.createProperty(propertyDTO);

        assertEquals("test name", result.getPropertyName());
        assertEquals("AVAILABLE", result.getPropertyStatus());
        assertEquals("123 Main St", result.getLocation());
        assertEquals(124400000.0, result.getPropertyValue());

        verify(propertyRepository).findByPropertyName("test name");
        verify(propertyRepository).save(any(PropertyEntity.class));
    }

    @Test
    void createPropertyPropertyAlreadyExistsTest() {

        PropertyDTO propertyDTO = new PropertyDTO();
        propertyDTO.setPropertyName("test name");

        PropertyEntity  propertyEntity = new PropertyEntity();
        propertyEntity.setId(1L);
        propertyEntity.setPropertyName("test name");

        when(propertyRepository.findByPropertyName(propertyDTO.getPropertyName())).
                thenReturn(Optional.of(propertyEntity));

        assertThrows(PropertyAlreadyExistsException.class,
                () -> propertyService.createProperty(propertyDTO));

        verify(propertyRepository).findByPropertyName("test name");
        verify(propertyRepository, never()).save(any());
    }

    @Test
    void getAllPropertiesSuccessTest() {

        PropertyEntity property1 = new PropertyEntity(
                "House 1",
                1000000.0,
                "AVAILABLE",
                "123 Main St");
        property1.setId(1L);

        PropertyEntity  property2 = new PropertyEntity(
                "House 2",
                4000000.0,
                "NOT AVAILABLE",
                "123 test St");
        property2.setId(2L);

        when(propertyRepository.findAll()).
                thenReturn(List.of(property1, property2));

        List<PropertyDTO> result = propertyService.getAllProperties();

        assertEquals(2, result.size());
        assertEquals("House 1", result.getFirst().getPropertyName());
        assertEquals("House 2", result.get(1).getPropertyName());
        assertEquals("123 Main St", result.getFirst().getLocation());
        assertEquals("123 test St", result.get(1).getLocation());

        verify(propertyRepository).findAll();
    }

    @Test
    void getAllPropertiesReturnsEmptyListTest() {

        when(propertyRepository.findAll()).thenReturn(List.of());

        List<PropertyDTO> result = propertyService.getAllProperties();

        assertTrue(result.isEmpty());
        verify(propertyRepository).findAll();
    }

    @Test
    void updatePropertySuccessTest() {
        PropertyEntity existingPropertyEntity = new PropertyEntity(
                "House 2",
                4000000.0,
                "NOT AVAILABLE",
                "123 test St");
        existingPropertyEntity.setId(1L);

        Long id = 1L;

        PropertyUpdateDTO propertyDTO = new PropertyUpdateDTO(
                "villa",
                6700000.0,
                "villa",
                "AVAILABLE",
                "sadaam@gmail.com",
                4,
                "456 test Blv"
        );

        when(propertyRepository.findById(id)).
                thenReturn(Optional.of(existingPropertyEntity));

        when(propertyRepository.save(any(PropertyEntity.class))).
                thenReturn(existingPropertyEntity);

        when(propertyRepository.findByPropertyName(propertyDTO.getPropertyName())).
                thenReturn(Optional.empty());

        PropertyDTO result = propertyService.updateProperty(id, propertyDTO);

        assertEquals("villa", result.getPropertyName());
        assertEquals("AVAILABLE", result.getPropertyStatus());
        assertEquals("456 test Blv", result.getLocation());
        assertEquals(6700000.0, result.getPropertyValue());

        verify(propertyRepository).save(any(PropertyEntity.class));
        verify(propertyRepository).findById(id);
        verify(propertyRepository).findByPropertyName(propertyDTO.getPropertyName());
    }
}
