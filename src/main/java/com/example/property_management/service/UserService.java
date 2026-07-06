package com.example.property_management.service;

import com.example.property_management.dto.*;

import java.util.List;

public interface UserService {

    List<AssignmentDTO> findByUser(Long id);


}
