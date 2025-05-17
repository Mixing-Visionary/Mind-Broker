package ru.visionary.mixing.mind_broker.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.visionary.mixing.mind_broker.entity.Image;
import ru.visionary.mixing.mind_broker.entity.Protection;
import ru.visionary.mixing.mind_broker.entity.User;
import ru.visionary.mixing.mind_broker.exception.ServiceException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LikeRepositoryTest extends AbstractRepositoryTest {
    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Test
    void save_ValidLike_ShouldPersist() {
        Long userId = userRepository.save(createTestUser());
        UUID imageId = imageRepository.save(createTestImage(userId));

        assertDoesNotThrow(() -> likeRepository.save(userId, imageId));
    }

    @Test
    void save_DuplicateLike_ShouldThrow() {
        Long userId = userRepository.save(createTestUser());
        UUID imageId = imageRepository.save(createTestImage(userId));

        likeRepository.save(userId, imageId);
        assertThrows(ServiceException.class, () -> likeRepository.save(userId, imageId));
    }

    @Test
    void deleteByUserAndImage_ShouldRemoveLike() {
        Long userId = userRepository.save(createTestUser());
        UUID imageId = imageRepository.save(createTestImage(userId));

        likeRepository.save(userId, imageId);
        assertDoesNotThrow(() -> likeRepository.deleteByUserAndImage(userId, imageId));
    }

    private User createTestUser() {
        return User.builder()
                .nickname("testuser")
                .email("test@example.com")
                .password("password")
                .build();
    }

    private Image createTestImage(Long ownerId) {
        return Image.builder()
                .owner(User.builder().id(ownerId).build())
                .protection(Protection.PUBLIC)
                .build();
    }
}