package ru.visionary.mixing.mind_broker.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.visionary.mixing.mind_broker.entity.RefreshToken;
import ru.visionary.mixing.mind_broker.entity.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class RefreshTokenRepositoryTest extends AbstractRepositoryTest {
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void saveAndFindByToken_ShouldWork() {
        User user = new User()
                .setNickname("tokenuser")
                .setEmail("token@example.com")
                .setPassword("pass");
        userRepository.save(user);

        RefreshToken token = new RefreshToken()
                .setToken("test-token")
                .setUser(user)
                .setExpiryDate(LocalDateTime.now().plusDays(1));

        RefreshToken saved = refreshTokenRepository.save(token);
        RefreshToken found = refreshTokenRepository.findByToken("test-token");

        assertNotNull(saved.getId());
        assertEquals("test-token", found.getToken());
        assertEquals(user.getId(), found.getUser().getId());
    }
}