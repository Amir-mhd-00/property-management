package com.example.property_management.repository;

import com.example.property_management.entity.AuditLogEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class AuditLogRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private AuditLogEntity persistLog(String entityName, String entityId, String action, String performedBy) {
        AuditLogEntity log = new AuditLogEntity();
        log.setEntityName(entityName);
        log.setEntityId(entityId);
        log.setAction(action);
        log.setPerformedBy(performedBy);
        log.setTimestamp(Instant.from(LocalDateTime.now()));
        return entityManager.persistFlushFind(log);
    }

    @Test
    void save_persistsAuditLogEntry() {
        AuditLogEntity saved = persistLog("Property", "1", "Create", "SYSTEM");

        assertNotNull(saved.getId());
        assertEquals("Property", saved.getEntityName());
        assertEquals("Create", saved.getAction());
    }

    @Test
    void findById_returnsMatchingLog() {
        AuditLogEntity saved = persistLog("Assignment", "5", "End", "SYSTEM");

        Optional<AuditLogEntity> found = auditLogRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Assignment", found.get().getEntityName());
        assertEquals("End", found.get().getAction());
    }

    // ---------- findByEntityNameAndEntityId ----------

    @Test
    void findByEntityNameAndEntityId_returnsMatchingLogsOnly() {
        persistLog("Property", "1", "Create", "SYSTEM");
        persistLog("Property", "1", "Update", "john@example.com");
        persistLog("Property", "2", "Create", "SYSTEM");
        persistLog("User", "1", "Create", "SYSTEM");

        List<AuditLogEntity> result = auditLogRepository.findByEntityNameAndEntityId("Property", "1");

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(l -> l.getEntityName().equals("Property") && l.getEntityId().equals("1")));
    }

    @Test
    void findByEntityNameAndEntityId_noMatches_returnsEmptyList() {
        persistLog("Property", "1", "Create", "SYSTEM");

        List<AuditLogEntity> result = auditLogRepository.findByEntityNameAndEntityId("Property", "999");

        assertTrue(result.isEmpty());
    }

    // ---------- findByPerformedBy ----------

    @Test
    void findByPerformedBy_returnsMatchingLogsOnly() {
        persistLog("Property", "1", "Create", "john@example.com");
        persistLog("User", "2", "Update", "john@example.com");
        persistLog("Assignment", "3", "End", "jane@example.com");

        List<AuditLogEntity> result = auditLogRepository.findByPerformedBy("john@example.com");

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(l -> l.getPerformedBy().equals("john@example.com")));
    }

    @Test
    void findByPerformedBy_noMatches_returnsEmptyList() {
        persistLog("Property", "1", "Create", "john@example.com");

        List<AuditLogEntity> result = auditLogRepository.findByPerformedBy("nobody@example.com");

        assertTrue(result.isEmpty());
    }

    @Test
    void save_nullOldAndNewValue_persistsSuccessfully() {
        AuditLogEntity log = new AuditLogEntity();
        log.setEntityName("Property");
        log.setEntityId("1");
        log.setAction("Create");
        log.setPerformedBy("SYSTEM");
        log.setOldValue(null);
        log.setNewValue(null);
        log.setTimestamp(Instant.from(LocalDateTime.now()));

        AuditLogEntity saved = entityManager.persistFlushFind(log);

        assertNull(saved.getOldValue());
        assertNull(saved.getNewValue());
    }
}
