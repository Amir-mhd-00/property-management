package com.example.property_management.service;

import com.example.property_management.dto.PropertyDTO;
import com.example.property_management.service.impl.PropertyServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class PropertyServiceImplTest {

    @Mock
    private PropertyService propertyService;
    @InjectMocks
    private PropertyServiceImpl propertyServiceImpl;
    @Test
    public void getPoropertyOrThrowTest() {
        PropertyDTO propertyDTO = new PropertyDTO();
    }
}
