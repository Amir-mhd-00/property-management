package com.example.property_management.service.impl;

import com.example.property_management.entity.AuditLogEntity;
import com.example.property_management.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void propertyLog_savesEntryWithSerializedValues() {
        auditLogService.propertyLog("Property", "1", "Create", "Created", "NewValue");

        ArgumentCaptor<AuditLogEntity> captor = ArgumentCaptor.forClass(AuditLogEntity.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLogEntity saved = captor.getValue();
        assertEquals("Property", saved.getEntityName());
        assertEquals("1", saved.getEntityId());
        assertEquals("Create", saved.getAction());
        assertEquals("\"Created\"", saved.getOldValue());
        assertEquals("\"NewValue\"", saved.getNewValue());
        assertEquals("SYSTEM", saved.getPerformedBy());
        assertNotNull(saved.getTimestamp());
    }

    @Test
    void userLog_savesEntry() {
        auditLogService.userLog("User", "5", "Delete", "before", "after");

        ArgumentCaptor<AuditLogEntity> captor = ArgumentCaptor.forClass(AuditLogEntity.class);
        verify(auditLogRepository).save(captor.capture());

        assertEquals("User", captor.getValue().getEntityName());
        assertEquals("Delete", captor.getValue().getAction());
    }

    @Test
    void assignmentLog_savesEntry() {
        auditLogService.assignmentLog("Assignment", "7", "End", "before", "after");

        ArgumentCaptor<AuditLogEntity> captor = ArgumentCaptor.forClass(AuditLogEntity.class);
        verify(auditLogRepository).save(captor.capture());

        assertEquals("Assignment", captor.getValue().getEntityName());
        assertEquals("End", captor.getValue().getAction());
    }

    @Test
    void log_nullValues_areStoredAsNull() {
        auditLogService.propertyLog("Property", "1", "Create", null, null);

        ArgumentCaptor<AuditLogEntity> captor = ArgumentCaptor.forClass(AuditLogEntity.class);
        verify(auditLogRepository).save(captor.capture());

        assertNull(captor.getValue().getOldValue());
        assertNull(captor.getValue().getNewValue());
    }

    @Test
    void log_usesAuthenticatedUsername_whenPresent() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("john@example.com", null));

        auditLogService.propertyLog("Property", "1", "Create", null, null);

        ArgumentCaptor<AuditLogEntity> captor = ArgumentCaptor.forClass(AuditLogEntity.class);
        verify(auditLogRepository).save(captor.capture());

        assertEquals("john@example.com", captor.getValue().getPerformedBy());
    }

    @Test
    void log_usesSystem_whenNoAuthentication() {
        SecurityContextHolder.clearContext();

        auditLogService.propertyLog("Property", "1", "Create", null, null);

        ArgumentCaptor<AuditLogEntity> captor = ArgumentCaptor.forClass(AuditLogEntity.class);
        verify(auditLogRepository).save(captor.capture());

        assertEquals("SYSTEM", captor.getValue().getPerformedBy());
    }

    @Test
    void log_capturesRemoteIp_whenRequestContextPresent() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.50");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        auditLogService.propertyLog("Property", "1", "Create", null, null);

        ArgumentCaptor<AuditLogEntity> captor = ArgumentCaptor.forClass(AuditLogEntity.class);
        verify(auditLogRepository).save(captor.capture());

        assertEquals("192.168.1.50", captor.getValue().getIpAddress());
    }

    @Test
    void log_nullIp_whenNoRequestContext() {
        RequestContextHolder.resetRequestAttributes();

        auditLogService.propertyLog("Property", "1", "Create", null, null);

        ArgumentCaptor<AuditLogEntity> captor = ArgumentCaptor.forClass(AuditLogEntity.class);
        verify(auditLogRepository).save(captor.capture());

        assertNull(captor.getValue().getIpAddress());
    }

    @Test
    void log_repositoryThrows_exceptionIsSwallowed() {
        when(auditLogRepository.save(any())).thenThrow(new RuntimeException("db down"));

        assertDoesNotThrow(() ->
                auditLogService.propertyLog("Property", "1", "Create", "old", "new"));
    }

    @Test
    void log_serializationFailure_isSwallowedGracefully() throws Exception {
        ObjectMapper failingMapper = mock(ObjectMapper.class);
        when(failingMapper.writeValueAsString(any()))
                .thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("boom") {});

        AuditLogServiceImpl serviceWithFailingMapper =
                new AuditLogServiceImpl(auditLogRepository, failingMapper);

        assertDoesNotThrow(() ->
                serviceWithFailingMapper.propertyLog("Property", "1", "Create", "old", "new"));

        verify(auditLogRepository, never()).save(any());
    }
}
