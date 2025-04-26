package ru.visionary.mixing.mind_broker.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.visionary.mixing.mind_broker.entity.RefreshToken;
import ru.visionary.mixing.mind_broker.entity.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
class RefreshTokenRepositoryTest extends AbstractRepositoryTest {
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void saveAndFindByToken_ShouldWork() {
        User user = userRepository.save(createTestUser());

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

    @Test
    void deleteByUser_ExistingTokens_RemovesAllTokens() {
        User user = userRepository.save(createTestUser());
        refreshTokenRepository.save(createTestToken(user, "token1"));
        refreshTokenRepository.save(createTestToken(user, "token2"));

        refreshTokenRepository.deleteByUser(user.getId());

        assertNull(refreshTokenRepository.findByToken("token1"));
        assertNull(refreshTokenRepository.findByToken("token2"));
    }

    private User createTestUser() {
        return new User()
                .setNickname("tokenuser")
                .setEmail("token@example.com")
                .setPassword("pass");
    }

    private RefreshToken createTestToken(User user, String token) {
        return new RefreshToken()
                .setToken(token)
                .setUser(user)
                .setExpiryDate(LocalDateTime.now().plusDays(1));
    }
}