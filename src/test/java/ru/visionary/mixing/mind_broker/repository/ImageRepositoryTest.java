package ru.visionary.mixing.mind_broker.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import ru.visionary.mixing.mind_broker.entity.Image;
import ru.visionary.mixing.mind_broker.entity.Protection;
import ru.visionary.mixing.mind_broker.entity.User;

import java.time.LocalDateTime;
import java.util.List;
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
        Long userId = userRepository.save(createTestUser());
        Image image = createTestImage(userId);

        UUID imageId = imageRepository.save(image);

        assertNotNull(imageId);
        assertFalse(imageId.toString().isEmpty());
    }

    @Test
    void save_ImageWithNonExistingUser_ShouldThrowException() {
        User nonExistingUser = User.builder().id(999L).build();
        Image image = Image.builder()
                .owner(nonExistingUser)
                .protection(Protection.PRIVATE)
                .createdAt(LocalDateTime.now())
                .build();

        assertThrows(DataIntegrityViolationException.class,
                () -> imageRepository.save(image));
    }

    @Test
    void save_ImageWithAllFields_ShouldStoreCorrectValues() {
        Long userId = userRepository.save(createTestUser());
        LocalDateTime createdAt = LocalDateTime.now().withNano(0);
        Image image = Image.builder()
                .owner(User.builder().id(userId).build())
                .protection(Protection.PRIVATE)
                .createdAt(createdAt)
                .build();

        UUID imageId = imageRepository.save(image);

        assertNotNull(imageId);
    }

    @Test
    void save_MultipleImages_ShouldGenerateUniqueIds() {
        Long userId = userRepository.save(createTestUser());
        Image image1 = createTestImage(userId);
        Image image2 = createTestImage(userId);

        UUID id1 = imageRepository.save(image1);
        UUID id2 = imageRepository.save(image2);

        assertNotEquals(id1, id2);
    }

    @Test
    void save_ShouldPersistImageWithAllFields() {
        Long userId = userRepository.save(createTestUser());
        Image image = Image.builder()
                .owner(User.builder().id(userId).build())
                .protection(Protection.PRIVATE)
                .createdAt(LocalDateTime.now())
                .build();

        UUID imageId = imageRepository.save(image);

        Image savedImage = imageRepository.findById(imageId);
        assertNotNull(savedImage);
        assertEquals(userId, savedImage.owner().id());
        assertEquals(Protection.PRIVATE, savedImage.protection());
        assertNotNull(savedImage.createdAt());
    }

    @Test
    void save_ShouldThrowExceptionWhenOwnerNotExists() {
        Image image = Image.builder()
                .owner(User.builder().id(999L).build())
                .protection(Protection.PUBLIC)
                .createdAt(LocalDateTime.now())
                .build();

        assertThrows(DataIntegrityViolationException.class,
                () -> imageRepository.save(image));
    }

    @Test
    void findById_ExistingImage_ReturnsImage() {
        Long userId = userRepository.save(createTestUser());
        UUID imageId = imageRepository.save(createTestImage(userId));

        Image found = imageRepository.findById(imageId);
        assertNotNull(found);
        assertEquals(imageId, found.id());
    }

    @Test
    void findByOwnerAndProtection_ReturnsPaginatedResults() {
        Long userId = userRepository.save(createTestUser());
        imageRepository.save(createTestImage(userId));
        imageRepository.save(createTestImage(userId));

        List<Image> result = imageRepository.findByOwnerAndProtection(userId, Protection.PUBLIC, 1, 0);

        assertEquals(1, result.size());
    }

    @Test
    void findByOwnerAndProtection_ShouldReturnPaginatedResults() {
        Long userId = userRepository.save(createTestUser());
        imageRepository.save(createTestImage(userId, Protection.PUBLIC));
        imageRepository.save(createTestImage(userId, Protection.PUBLIC));
        imageRepository.save(createTestImage(userId, Protection.PRIVATE));

        List<Image> result = imageRepository.findByOwnerAndProtection(
                userId, Protection.PUBLIC, 1, 0
        );

        assertEquals(1, result.size());
        assertEquals(Protection.PUBLIC, result.get(0).protection());
    }

    @Test
    void updateProtection_ValidRequest_UpdatesDatabase() {
        Long userId = userRepository.save(createTestUser());
        UUID imageId = imageRepository.save(createTestImage(userId));

        imageRepository.updateProtection(imageId, Protection.PRIVATE);

        Image updated = imageRepository.findById(imageId);
        assertEquals(Protection.PRIVATE, updated.protection());
    }

    @Test
    void updateProtection_ShouldModifyExistingRecord() {
        Long userId = userRepository.save(createTestUser());
        UUID imageId = imageRepository.save(createTestImage(userId, Protection.PUBLIC));

        imageRepository.updateProtection(imageId, Protection.PRIVATE);

        Image updatedImage = imageRepository.findById(imageId);
        assertEquals(Protection.PRIVATE, updatedImage.protection());
    }

    @Test
    void deleteById_ExistingImage_RemovesFromDatabase() {
        Long userId = userRepository.save(createTestUser());
        UUID imageId = imageRepository.save(createTestImage(userId));

        imageRepository.deleteById(imageId);

        assertNull(imageRepository.findById(imageId));
    }

    @Test
    void deleteById_ShouldRemoveImageFromDatabase() {
        Long userId = userRepository.save(createTestUser());
        UUID imageId = imageRepository.save(createTestImage(userId));

        imageRepository.deleteById(imageId);

        assertNull(imageRepository.findById(imageId));
    }

    private User createTestUser() {
        return User.builder()
                .nickname("testuser")
                .email("test@example.com")
                .password("password")
                .build();
    }

    private Image createTestImage(Long userId) {
        return Image.builder()
                .owner(User.builder().id(userId).build())
                .protection(Protection.PUBLIC)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Image createTestImage(Long userId, Protection protection) {
        return Image.builder()
                .owner(User.builder().id(userId).build())
                .protection(protection)
                .createdAt(LocalDateTime.now())
                .build();
    }
}