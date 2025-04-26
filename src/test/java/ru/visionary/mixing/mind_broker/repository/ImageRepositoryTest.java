package ru.visionary.mixing.mind_broker.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import ru.visionary.mixing.mind_broker.entity.Image;
import ru.visionary.mixing.mind_broker.entity.Protection;
import ru.visionary.mixing.mind_broker.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class ImageRepositoryTest extends AbstractRepositoryTest {
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void save_ValidImage_ShouldPersistInDatabase() {
        User user = userRepository.save(createTestUser());
        Image image = new Image()
                .setOwner(user)
                .setProtection(Protection.PUBLIC)
                .setCreatedAt(LocalDateTime.now());

        UUID imageId = imageRepository.save(image);

        assertNotNull(imageId);
        assertFalse(imageId.toString().isEmpty());
    }

    @Test
    void save_ImageWithNonExistingUser_ShouldThrowException() {
        User nonExistingUser = new User().setId(999L);
        Image image = new Image()
                .setOwner(nonExistingUser)
                .setProtection(Protection.PRIVATE)
                .setCreatedAt(LocalDateTime.now());

        assertThrows(DataIntegrityViolationException.class,
                () -> imageRepository.save(image));
    }

    @Test
    void save_ImageWithAllFields_ShouldStoreCorrectValues() {
        User user = userRepository.save(createTestUser());
        LocalDateTime createdAt = LocalDateTime.now().withNano(0);
        Image image = new Image()
                .setOwner(user)
                .setProtection(Protection.PRIVATE)
                .setCreatedAt(createdAt);

        UUID imageId = imageRepository.save(image);

        assertNotNull(imageId);
    }

    @Test
    void save_MultipleImages_ShouldGenerateUniqueIds() {
        User user = userRepository.save(createTestUser());
        Image image1 = createTestImage(user);
        Image image2 = createTestImage(user);

        UUID id1 = imageRepository.save(image1);
        UUID id2 = imageRepository.save(image2);

        assertNotEquals(id1, id2);
    }

    @Test
    void findById_ExistingImage_ReturnsImage() {
        User user = userRepository.save(createTestUser());
        UUID imageId = imageRepository.save(createTestImage(user));

        Image found = imageRepository.findById(imageId);
        assertNotNull(found);
        assertEquals(imageId, found.getId());
    }

    @Test
    void updateProtection_ValidRequest_UpdatesDatabase() {
        User user = userRepository.save(createTestUser());
        UUID imageId = imageRepository.save(createTestImage(user));

        imageRepository.updateProtection(imageId, Protection.PRIVATE);

        Image updated = imageRepository.findById(imageId);
        assertEquals(Protection.PRIVATE, updated.getProtection());
    }

    @Test
    void deleteById_ExistingImage_RemovesFromDatabase() {
        User user = userRepository.save(createTestUser());
        UUID imageId = imageRepository.save(createTestImage(user));

        imageRepository.deleteById(imageId);

        assertNull(imageRepository.findById(imageId));
    }

    private User createTestUser() {
        return new User()
                .setNickname("testuser")
                .setEmail("test@example.com")
                .setPassword("password");
    }

    private Image createTestImage(User user) {
        return new Image()
                .setOwner(user)
                .setProtection(Protection.PUBLIC)
                .setCreatedAt(LocalDateTime.now());
    }
}