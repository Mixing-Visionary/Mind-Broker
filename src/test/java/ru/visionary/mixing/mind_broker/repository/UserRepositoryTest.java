package ru.visionary.mixing.mind_broker.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.visionary.mixing.mind_broker.entity.User;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
class UserRepositoryTest extends AbstractRepositoryTest {
    @Autowired
    private UserRepository userRepository;

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
        return User.builder()
                .nickname("testuser")
                .email("test@example.com")
                .password("password")
                .build();
    }
}
