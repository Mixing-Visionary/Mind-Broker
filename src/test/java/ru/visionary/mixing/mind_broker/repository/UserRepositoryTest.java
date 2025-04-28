package ru.visionary.mixing.mind_broker.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import ru.visionary.mixing.mind_broker.entity.User;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class UserRepositoryTest extends AbstractRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    void save_ShouldPersistUserWithRequiredFields() {
        User user = User.builder()
                .nickname("testuser")
                .email("test@example.com")
                .password("encoded-password")
                .build();

        Long userId = userRepository.save(user);
        User savedUser = userRepository.findById(userId);

        assertNotNull(savedUser);
        assertEquals("testuser", savedUser.nickname().toLowerCase());
        assertEquals("test@example.com", savedUser.email().toLowerCase());
        assertTrue(savedUser.active());
    }

    @Test
    void save_ShouldThrowOnDuplicateEmail() {
        userRepository.save(createTestUser("user1", "test@example.com"));

        User duplicate = createTestUser("user2", "TEST@EXAMPLE.COM");
        assertThrows(DataIntegrityViolationException.class,
                () -> userRepository.save(duplicate));
    }

    @Test
    void findByEmail_ExistingUser_ReturnsUser() {
        User user = User.builder()
                .nickname("existing")
                .email("exist@example.com")
                .password("pass")
                .build();
        userRepository.save(user);

        User found = userRepository.findByEmail("exist@example.com");

        assertNotNull(found);
        assertEquals("existing", found.nickname());
        assertEquals("exist@example.com", found.email());
    }

    @Test
    void findByEmail_ShouldBeCaseInsensitive() {
        userRepository.save(User.builder()
                .nickname("user1")
                .email("Test@Example.COM")
                .password("pass")
                .build());

        User found = userRepository.findByEmail("TEST@eXample.com");

        assertNotNull(found);
        assertEquals("test@example.com", found.email());
    }

    @Test
    void findByEmailOrNickname_ExistingUser_ReturnsUser() {
        User user = User.builder()
                .nickname("user1")
                .email("user1@example.com")
                .password("pass")
                .build();
        userRepository.save(user);

        User found = userRepository.findByEmailOrNickname("user1@example.com", "user1");
        assertNotNull(found);
        assertEquals("user1", found.nickname());
    }

    @Test
    void updateUser_PartialUpdate_UpdatesOnlySpecifiedFields() {
        Long userId = userRepository.save(createTestUser());
        userRepository.updateUser(userId, "newnick", null, null, null);

        User updated = userRepository.findById(userId);
        assertEquals("newnick", updated.nickname());
        assertNotNull(updated.password());
    }

    @Test
    void deleteUser_SetsInactive() {
        Long userId = userRepository.save(createTestUser());
        userRepository.deleteUser(userId);

        User deleted = userRepository.findById(userId);
        assertFalse(deleted.active());
    }

    @Test
    void deleteAvatar_RemovesAvatarUuid() {
        Long userId = userRepository.save(createTestUser());
        userRepository.updateUser(userId, null, null, null, UUID.randomUUID());

        userRepository.deleteAvatar(userId);
        User user = userRepository.findById(userId);
        assertNull(user.avatar());
    }

    private User createTestUser() {
        return createTestUser("testuser", "test@example.com");
    }

    private User createTestUser(String nickname, String email) {
        return User.builder()
                .nickname(nickname)
                .email(email)
                .password("password")
                .build();
    }
}
