package com.example.property_management.repository;

import com.example.property_management.config.JpaConfig;
import com.example.property_management.entity.AssignmentEntity;
import com.example.property_management.entity.PropertyEntity;
import com.example.property_management.entity.UserEntity;
import com.example.property_management.enums.AssignmentRole;
import com.example.property_management.enums.AssignmentStatus;
import com.example.property_management.enums.PropertyStatus;
import com.example.property_management.enums.PropertyType;
import com.example.property_management.enums.UserRole;
import com.example.property_management.security.SpringSecurityAuditorAware;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import({JpaConfig.class, SpringSecurityAuditorAware.class})
class AssignmentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AssignmentRepository assignmentRepository;

    private UserEntity persistUser(String email, UserRole role) {
        UserEntity user = new UserEntity();
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail(email);
        user.setPassword("pw");
        user.setRole(role);
        return entityManager.persistFlushFind(user);
    }

    private PropertyEntity persistProperty(String name, UserEntity owner) {
        PropertyEntity property = new PropertyEntity();
        property.setPropertyName(name);
        property.setPropertyValue(100000.0);
        property.setPropertyType(PropertyType.HOUSE);
        property.setPropertyStatus(PropertyStatus.AVAILABLE);
        property.setLocation("Test location");
        property.setOwner(owner);
        return entityManager.persistFlushFind(property);
    }

    private AssignmentEntity persistAssignment(PropertyEntity property, UserEntity agent, AssignmentStatus status) {
        AssignmentEntity assignment = new AssignmentEntity();
        assignment.setProperty(property);
        assignment.setUser(agent);
        assignment.setRole(AssignmentRole.PROPERTY_MANAGER);
        assignment.setStatus(status);
        return entityManager.persistFlushFind(assignment);// return value never used
    }

    @Test
    void findAllByProperty_id_returnsAssignmentsForProperty() {
        UserEntity owner = persistUser("owner@example.com", UserRole.OWNER);
        UserEntity agent = persistUser("agent@example.com", UserRole.AGENT);
        PropertyEntity property = persistProperty("Villa 1", owner);
        persistAssignment(property, agent, AssignmentStatus.ACTIVE);

        List<AssignmentEntity> result = assignmentRepository.findAllByProperty_id(property.getId());

        assertEquals(1, result.size());
        assertEquals(property.getId(), result.getFirst().getProperty().getId());
    }

    @Test
    void findAllByProperty_id_noAssignments_returnsEmptyList() {
        UserEntity owner = persistUser("owner_empty@example.com", UserRole.OWNER);
        PropertyEntity property = persistProperty("Villa Empty", owner);

        List<AssignmentEntity> result = assignmentRepository.findAllByProperty_id(property.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void findAllByUser_id_returnsAssignmentsForUser() {
        UserEntity owner = persistUser("owner2@example.com", UserRole.OWNER);
        UserEntity agent = persistUser("agent2@example.com", UserRole.AGENT);
        PropertyEntity property = persistProperty("Villa 2", owner);
        persistAssignment(property, agent, AssignmentStatus.ACTIVE);

        List<AssignmentEntity> result = assignmentRepository.findAllByUser_id(agent.getId());

        assertEquals(1, result.size());
        assertEquals(agent.getId(), result.getFirst().getUser().getId());
    }

    @Test
    void findByProperty_idAndStatus_returnsActiveAssignment() {
        UserEntity owner = persistUser("owner3@example.com", UserRole.OWNER);
        UserEntity agent = persistUser("agent3@example.com", UserRole.AGENT);
        PropertyEntity property = persistProperty("Villa 3", owner);
        persistAssignment(property, agent, AssignmentStatus.ACTIVE);

        Optional<AssignmentEntity> result =
                assignmentRepository.findByProperty_idAndStatus(property.getId(), AssignmentStatus.ACTIVE);

        assertTrue(result.isPresent());
        assertEquals(AssignmentStatus.ACTIVE, result.get().getStatus());
    }

    @Test
    void findByProperty_idAndStatus_noMatchingStatus_returnsEmpty() {
        UserEntity owner = persistUser("owner4@example.com", UserRole.OWNER);
        UserEntity agent = persistUser("agent4@example.com", UserRole.AGENT);
        PropertyEntity property = persistProperty("Villa 4", owner);
        persistAssignment(property, agent, AssignmentStatus.INACTIVE);

        Optional<AssignmentEntity> result =
                assignmentRepository.findByProperty_idAndStatus(property.getId(), AssignmentStatus.ACTIVE);

        assertTrue(result.isEmpty());
    }

    @Test
    void existsByUserIdAndPropertyId_matching_returnsTrue() {
        UserEntity owner = persistUser("owner5@example.com", UserRole.OWNER);
        UserEntity agent = persistUser("agent5@example.com", UserRole.AGENT);
        PropertyEntity property = persistProperty("Villa 5", owner);
        persistAssignment(property, agent, AssignmentStatus.ACTIVE);

        assertTrue(assignmentRepository.existsByUserIdAndPropertyId(agent.getId(), property.getId()));
    }

    @Test
    void existsByUserIdAndPropertyId_nonMatching_returnsFalse() {
        UserEntity owner = persistUser("owner6@example.com", UserRole.OWNER);
        UserEntity agent = persistUser("agent6@example.com", UserRole.AGENT);
        UserEntity otherAgent = persistUser("agent7@example.com", UserRole.AGENT);
        PropertyEntity property = persistProperty("Villa 6", owner);
        persistAssignment(property, agent, AssignmentStatus.ACTIVE);

        assertFalse(assignmentRepository.existsByUserIdAndPropertyId(otherAgent.getId(), property.getId()));
    }
}
