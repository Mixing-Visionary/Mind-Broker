package ru.visionary.mixing.mind_broker.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.visionary.mixing.mind_broker.entity.RefreshToken;
import ru.visionary.mixing.mind_broker.entity.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
class RefreshTokenRepositoryTest extends AbstractRepositoryTest {
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void saveAndFindByToken_ShouldWork() {
        Long userId = userRepository.save(createTestUser());

        RefreshToken token = RefreshToken.builder()
                .token("test-token")
                .user(User.builder().id(userId).build())
                .expiryDate(LocalDateTime.now().plusDays(1))
                .build();

        refreshTokenRepository.save(token);
        RefreshToken found = refreshTokenRepository.findByToken("test-token");

        assertEquals("test-token", found.token());
        assertEquals(userId, found.user().id());
    }

    @Test
    void deleteByUser_ExistingTokens_RemovesAllTokens() {
        Long userId = userRepository.save(createTestUser());
        refreshTokenRepository.save(createTestToken(userId, "token1"));
        refreshTokenRepository.save(createTestToken(userId, "token2"));

        refreshTokenRepository.deleteByUser(userId);

        assertNull(refreshTokenRepository.findByToken("token1"));
        assertNull(refreshTokenRepository.findByToken("token2"));
    }

    private User createTestUser() {
        return User.builder()
                .nickname("tokenuser")
                .email("token@example.com")
                .password("pass")
                .build();
    }

    private RefreshToken createTestToken(Long userId, String token) {
        return RefreshToken.builder()
                .token(token)
                .user(User.builder().id(userId).build())
                .expiryDate(LocalDateTime.now().plusDays(1))
                .build();
    }
}