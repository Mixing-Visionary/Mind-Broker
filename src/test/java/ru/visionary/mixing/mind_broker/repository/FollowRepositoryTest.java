package ru.visionary.mixing.mind_broker.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.visionary.mixing.mind_broker.entity.Follow;
import ru.visionary.mixing.mind_broker.entity.User;
import ru.visionary.mixing.mind_broker.exception.ServiceException;

import static org.junit.jupiter.api.Assertions.*;

class FollowRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void save_ValidFollow_ShouldPersist() {
        Long followerId = userRepository.save(createTestUser("follower"));
        Long followId = userRepository.save(createTestUser("followed"));

        Follow follow = Follow.builder()
                .follower(User.builder().id(followerId).build())
                .follow(User.builder().id(followId).build())
                .build();

        assertDoesNotThrow(() -> followRepository.save(follow));
    }

    @Test
    void save_DuplicateFollow_ShouldThrowException() {
        Long followerId = userRepository.save(createTestUser("follower"));
        Long followId = userRepository.save(createTestUser("followed"));

        Follow follow = Follow.builder()
                .follower(User.builder().id(followerId).build())
                .follow(User.builder().id(followId).build())
                .build();

        followRepository.save(follow);
        assertThrows(ServiceException.class, () -> followRepository.save(follow),
                "Should throw ALREADY_FOLLOWING on duplicate");
    }

    @Test
    void deleteByFollowerAndFollow_ExistingFollow_ShouldDelete() {
        Long followerId = userRepository.save(createTestUser("follower"));
        Long followId = userRepository.save(createTestUser("followed"));

        followRepository.save(createTestFollow(followerId, followId));
        int deleted = followRepository.deleteByFollowerAndFollow(followerId, followId);

        assertEquals(1, deleted, "Should delete exactly 1 row");
    }

    @Test
    void deleteByFollowerAndFollow_NonExistingFollow_ShouldReturnZero() {
        int deleted = followRepository.deleteByFollowerAndFollow(1L, 2L);
        assertEquals(0, deleted, "Should return 0 when nothing to delete");
    }

    private User createTestUser(String prefix) {
        return User.builder()
                .nickname(prefix + "-user")
                .email(prefix + "@example.com")
                .password("password")
                .build();
    }

    private Follow createTestFollow(Long followerId, Long followId) {
        return Follow.builder()
                .follower(User.builder().id(followerId).build())
                .follow(User.builder().id(followId).build())
                .build();
    }
}