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
}
