package ru.visionary.mixing.mind_broker.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.visionary.mixing.mind_broker.entity.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class UserRepositoryTest extends AbstractRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    void save_NewUser_ReturnsWithId() {
        User user = new User()
                .setNickname("testuser")
                .setEmail("test@example.com")
                .setPassword("password");

        User saved = userRepository.save(user);

        assertNotNull(saved.getId());
        assertEquals("testuser", saved.getNickname());
    }

    @Test
    void findByEmail_ExistingUser_ReturnsUser() {
        User user = new User()
                .setNickname("existing")
                .setEmail("exist@example.com")
                .setPassword("pass");
        userRepository.save(user);

        User found = userRepository.findByEmail("exist@example.com");

        assertNotNull(found);
        assertEquals("existing", found.getNickname());
        assertEquals("exist@example.com", found.getEmail());
    }
}
