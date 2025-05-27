package ru.visionary.mixing.mind_broker.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.visionary.mixing.mind_broker.entity.Follow;
import ru.visionary.mixing.mind_broker.entity.Image;
import ru.visionary.mixing.mind_broker.entity.Protection;
import ru.visionary.mixing.mind_broker.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class FeedRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private FeedRepository feedRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private FollowRepository followRepository;

    @Test
    void getFeedByNew_ReturnsPublicImages() {
        Long userId = userRepository.save(createUser());
        imageRepository.save(createImage(userId, Protection.PUBLIC));
        imageRepository.save(createImage(userId, Protection.PRIVATE));

        List<Image> result = feedRepository.getFeedByNew(null, 10, 0);

        assertEquals(1, result.size());
        assertEquals(Protection.PUBLIC, result.get(0).protection());
    }

    @Test
    void getFeedByPopular_OrdersByLikes() {
        Long userId = userRepository.save(createUser("test"));
        UUID image1 = imageRepository.save(createImage(userId, Protection.PUBLIC));
        UUID image2 = imageRepository.save(createImage(userId, Protection.PUBLIC));

        Long user1 = userRepository.save(createUser("user1"));
        Long user2 = userRepository.save(createUser("user2"));

        likeRepository.save(user1, image2);
        likeRepository.save(user2, image2);

        List<Image> result = feedRepository.getFeedByPopular(null, 10, 0);
        assertEquals(2, result.size());
        assertEquals(image2, result.get(0).id());
    }

    @Test
    void getFeedByFollow_ReturnsFollowedUsers() {
        Long currentUser = userRepository.save(createUser("current"));
        Long followedUser = userRepository.save(createUser("followed"));

        followRepository.save(createFollow(currentUser, followedUser));
        imageRepository.save(createImage(followedUser, Protection.PUBLIC));

        List<Image> result = feedRepository.getFeedByFollow(currentUser, 10, 0);

        assertEquals(1, result.size());
    }

    private User createUser() {
        return User.builder()
                .nickname("testuser")
                .email("test@example.com")
                .password("password")
                .build();
    }

    private User createUser(String prefix) {
        return User.builder()
                .nickname(prefix + "-user")
                .email(prefix + "@example.com")
                .password("password")
                .build();
    }

    private Image createImage(Long ownerId, Protection protection) {
        return Image.builder()
                .owner(User.builder().id(ownerId).build())
                .protection(protection)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Follow createFollow(Long follower, Long follow) {
        return Follow.builder()
                .follower(User.builder().id(follower).build())
                .follow(User.builder().id(follow).build())
                .build();
    }
}