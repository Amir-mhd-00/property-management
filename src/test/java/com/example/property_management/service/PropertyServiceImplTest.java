package com.example.property_management.service;

import com.example.property_management.dto.PropertyDTO;
import com.example.property_management.entity.PropertyEntity;
import com.example.property_management.error.exception.PropertyNotFoundException;
import com.example.property_management.repository.PropertyRepository;
import com.example.property_management.service.impl.PropertyServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

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
                thenReturn(Optional.empty());

        

        assertThrows(PropertyNotFoundException.class,
                () -> propertyService.getProperty(1L));

        verify(propertyRepository).findById(1L);
    }

    @Test
    void createPropertySuccessTest() {

        PropertyDTO propertyDTO = new PropertyDTO();
        propertyDTO.setPropertyName("test name");
        propertyDTO.setPropertyValue(124400000.0);
        propertyDTO.setPropertyStatus("AVAILABLE");
        propertyDTO.setLocation("123 Main St");

        PropertyEntity  propertyEntity = new PropertyEntity();
        propertyEntity.setPropertyName("test name");
        propertyEntity.setPropertyValue(124400000.0);
        propertyEntity.setPropertyStatus("AVAILABLE");
        propertyEntity.setLocation("123 Main St");

        when(propertyRepository.findByPropertyName(propertyDTO.getPropertyName())).
                thenReturn(Optional.empty());
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
    void createPropertyFailureTest() {}

    @Test
    void createPropertyPropertyAlreadyExistsTest() {}
}
