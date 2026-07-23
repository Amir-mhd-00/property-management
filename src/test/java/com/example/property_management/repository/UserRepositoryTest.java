package com.example.property_management.repository;

import com.example.property_management.config.JpaConfig;
import com.example.property_management.entity.UserEntity;
import com.example.property_management.enums.UserRole;
import com.example.property_management.security.SpringSecurityAuditorAware;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import({JpaConfig.class, SpringSecurityAuditorAware.class})
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private UserEntity persistUser(String email, UserRole role) {
        UserEntity user = new UserEntity();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail(email);
        user.setPhone("123456789");
        user.setPassword("encoded");
        user.setRole(role);
        return entityManager.persistFlushFind(user);
    }

    @Test
    void findByEmail_userExists_returnsUser() {
        persistUser("john@example.com", UserRole.GUEST);

        Optional<UserEntity> result = userRepository.findByEmail("john@example.com");

        assertTrue(result.isPresent());
        assertEquals("john@example.com", result.get().getEmail());
    }

    @Test
    void findByEmail_userDoesNotExist_returnsEmpty() {
        Optional<UserEntity> result = userRepository.findByEmail("missing@example.com");

        assertTrue(result.isEmpty());
    }

    @Test
    void existsByEmail_userExists_returnsTrue() {
        persistUser("jane@example.com", UserRole.AGENT);

        assertTrue(userRepository.existsByEmail("jane@example.com"));
    }

    @Test
    void existsByEmail_userDoesNotExist_returnsFalse() {
        assertFalse(userRepository.existsByEmail("nobody@example.com"));
    }

    @Test
    void save_setsAuditingTimestamps() {
        UserEntity saved = persistUser("audit@example.com", UserRole.OWNER);

        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void save_duplicateEmail_violatesUniqueConstraint() {
        persistUser("dup@example.com", UserRole.GUEST);

        UserEntity duplicate = new UserEntity();
        duplicate.setFirstName("Jane");
        duplicate.setLastName("Smith");
        duplicate.setEmail("dup@example.com");
        duplicate.setPassword("pw");
        duplicate.setRole(UserRole.GUEST);

        assertThrows(Exception.class, () -> entityManager.persistAndFlush(duplicate));
    }

    @Test
    void findById_returnsUser() {
        UserEntity saved = persistUser("findme@example.com", UserRole.MANAGER);

        Optional<UserEntity> found = userRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(UserRole.MANAGER, found.get().getRole());
    }
}
