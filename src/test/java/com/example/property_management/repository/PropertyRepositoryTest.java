package com.example.property_management.repository;

import com.example.property_management.config.JpaConfig;
import com.example.property_management.entity.PropertyEntity;
import com.example.property_management.entity.UserEntity;
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
class PropertyRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PropertyRepository propertyRepository;

    private UserEntity persistOwner(String email) {
        UserEntity owner = new UserEntity();
        owner.setFirstName("Owner");
        owner.setLastName("Test");
        owner.setEmail(email);
        owner.setPassword("pw");
        owner.setRole(UserRole.OWNER);
        return entityManager.persistFlushFind(owner);
    }

    private PropertyEntity persistProperty(String name, PropertyStatus status, UserEntity owner) {
        PropertyEntity property = new PropertyEntity();
        property.setPropertyName(name);
        property.setPropertyValue(150000.0);
        property.setPropertyType(PropertyType.HOUSE);
        property.setPropertyStatus(status);
        property.setRooms(3);
        property.setLocation("123 Test St");
        property.setOwner(owner);
        return entityManager.persistFlushFind(property);
    }

    @Test
    void findByPropertyName_exists_returnsProperty() {
        UserEntity owner = persistOwner("owner1@example.com");
        persistProperty("Sunset Villa", PropertyStatus.AVAILABLE, owner);

        Optional<PropertyEntity> result = propertyRepository.findByPropertyName("Sunset Villa");

        assertTrue(result.isPresent());
        assertEquals("Sunset Villa", result.get().getPropertyName());
    }

    @Test
    void findByPropertyName_notExists_returnsEmpty() {
        Optional<PropertyEntity> result = propertyRepository.findByPropertyName("Nonexistent");

        assertTrue(result.isEmpty());
    }

    @Test
    void findAllByPropertyStatus_returnsMatchingProperties() {
        UserEntity owner = persistOwner("owner2@example.com");
        persistProperty("Villa A", PropertyStatus.AVAILABLE, owner);
        persistProperty("Villa B", PropertyStatus.SOLD, owner);
        persistProperty("Villa C", PropertyStatus.AVAILABLE, owner);

        List<PropertyEntity> result = propertyRepository.findAllByPropertyStatus(PropertyStatus.AVAILABLE);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(p -> p.getPropertyStatus() == PropertyStatus.AVAILABLE));
    }

    @Test
    void existsByIdAndOwnerId_matchingOwner_returnsTrue() {
        UserEntity owner = persistOwner("owner3@example.com");
        PropertyEntity property = persistProperty("Villa D", PropertyStatus.AVAILABLE, owner);

        assertTrue(propertyRepository.existsByIdAndOwnerId(property.getId(), owner.getId()));
    }

    @Test
    void existsByIdAndOwnerId_nonMatchingOwner_returnsFalse() {
        UserEntity owner = persistOwner("owner4@example.com");
        UserEntity otherOwner = persistOwner("owner5@example.com");
        PropertyEntity property = persistProperty("Villa E", PropertyStatus.AVAILABLE, owner);

        assertFalse(propertyRepository.existsByIdAndOwnerId(property.getId(), otherOwner.getId()));
    }

    @Test
    void findAllByOwnerId_returnsOwnersProperties() {
        UserEntity owner = persistOwner("owner6@example.com");
        UserEntity otherOwner = persistOwner("owner7@example.com");
        persistProperty("Villa F", PropertyStatus.AVAILABLE, owner);
        persistProperty("Villa G", PropertyStatus.AVAILABLE, owner);
        persistProperty("Villa H", PropertyStatus.AVAILABLE, otherOwner);

        List<PropertyEntity> result = propertyRepository.findAllByOwnerId(owner.getId());

        assertEquals(2, result.size());
    }

    @Test
    void save_duplicatePropertyName_violatesUniqueConstraint() {
        UserEntity owner = persistOwner("owner8@example.com");
        persistProperty("Unique Villa", PropertyStatus.AVAILABLE, owner);

        PropertyEntity duplicate = new PropertyEntity();
        duplicate.setPropertyName("Unique Villa");
        duplicate.setPropertyValue(1000.0);
        duplicate.setPropertyType(PropertyType.HOUSE);
        duplicate.setPropertyStatus(PropertyStatus.AVAILABLE);
        duplicate.setLocation("Another location");
        duplicate.setOwner(owner);

        assertThrows(Exception.class, () -> entityManager.persistAndFlush(duplicate));
    }
}
