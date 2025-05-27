package ru.visionary.mixing.mind_broker.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.visionary.mixing.mind_broker.entity.Follow;
import ru.visionary.mixing.mind_broker.entity.User;
import ru.visionary.mixing.mind_broker.exception.ServiceException;

import java.util.List;

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

    @Test
    void getFollows_ShouldReturnPaginatedResults() {
        Long followerId = userRepository.save(createTestUser("follower"));
        Long follow1Id = userRepository.save(createTestUser("follow1"));
        Long follow2Id = userRepository.save(createTestUser("follow2"));

        followRepository.save(createTestFollow(followerId, follow1Id));
        followRepository.save(createTestFollow(followerId, follow2Id));

        List<User> followsPage1 = followRepository.getFollows(followerId, 1, 0);
        List<User> followsPage2 = followRepository.getFollows(followerId, 1, 1);

        assertEquals(1, followsPage1.size());
        assertEquals(1, followsPage2.size());
        assertNotEquals(followsPage1.get(0).id(), followsPage2.get(0).id());
    }

    @Test
    void getFollowers_ShouldReturnPaginatedResults() {
        Long user1Id = userRepository.save(createTestUser("user1"));
        Long user2Id = userRepository.save(createTestUser("user2"));
        Long targetUserId = userRepository.save(createTestUser("target"));

        followRepository.save(createTestFollow(user1Id, targetUserId));
        followRepository.save(createTestFollow(user2Id, targetUserId));

        List<User> followersPage1 = followRepository.getFollowers(targetUserId, 1, 0);
        List<User> followersPage2 = followRepository.getFollowers(targetUserId, 1, 1);

        assertEquals(1, followersPage1.size());
        assertEquals(1, followersPage2.size());
        assertNotEquals(followersPage1.get(0).id(), followersPage2.get(0).id());
    }

    @Test
    void getFollows_ShouldReturnEmptyListForNoFollows() {
        Long userId = userRepository.save(createTestUser("user"));
        List<User> follows = followRepository.getFollows(userId, 10, 0);
        assertTrue(follows.isEmpty());
    }

    @Test
    void getFollowers_ShouldReturnEmptyListForNoFollowers() {
        Long userId = userRepository.save(createTestUser("user"));
        List<User> followers = followRepository.getFollowers(userId, 10, 0);
        assertTrue(followers.isEmpty());
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