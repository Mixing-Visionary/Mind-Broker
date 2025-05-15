package ru.visionary.mixing.mind_broker.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.visionary.mixing.mind_broker.entity.Comment;
import ru.visionary.mixing.mind_broker.entity.Image;
import ru.visionary.mixing.mind_broker.entity.Protection;
import ru.visionary.mixing.mind_broker.entity.User;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommentRepositoryTest extends AbstractRepositoryTest {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Test
    void save_ValidComment_ShouldPersist() {
        Long userId = userRepository.save(createTestUser());
        UUID imageId = imageRepository.save(createTestImage(userId));

        Comment comment = Comment.builder()
                .author(User.builder().id(userId).build())
                .image(Image.builder().id(imageId).build())
                .comment("Test comment")
                .build();

        commentRepository.save(comment);

        List<Comment> comments = commentRepository.findByImage(imageId, 10, 0);
        assertEquals(1, comments.size());
        assertEquals("Test comment", comments.get(0).comment());
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