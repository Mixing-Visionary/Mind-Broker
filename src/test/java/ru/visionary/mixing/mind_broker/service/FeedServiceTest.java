package ru.visionary.mixing.mind_broker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.visionary.mixing.generated.model.GetImagesResponse;
import ru.visionary.mixing.generated.model.ImageResponse;
import ru.visionary.mixing.mind_broker.entity.Image;
import ru.visionary.mixing.mind_broker.entity.Protection;
import ru.visionary.mixing.mind_broker.entity.User;
import ru.visionary.mixing.mind_broker.exception.ErrorCode;
import ru.visionary.mixing.mind_broker.exception.ServiceException;
import ru.visionary.mixing.mind_broker.repository.FeedRepository;
import ru.visionary.mixing.mind_broker.service.mapper.ImageMapperImpl;
import ru.visionary.mixing.mind_broker.utils.SecurityContextUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {
    @Mock
    private FeedRepository feedRepository;
    @Spy
    private ImageMapperImpl imageMapper;

    @InjectMocks
    private FeedService feedService;

    @Test
    void getFeed_NewSort_CallsCorrectMethod() {
        when(feedRepository.getFeedByNew(10, 0))
                .thenReturn(Collections.emptyList());

        GetImagesResponse response = feedService.getFeed("NEW", 10, 0);

        assertNotNull(response);
        verify(feedRepository).getFeedByNew(10, 0);
    }

    @Test
    void getFeed_FollowSort_Unauthorized_ThrowsException() {
        try (var utils = mockStatic(SecurityContextUtils.class)) {
            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(null);

            ServiceException ex = assertThrows(
                    ServiceException.class,
                    () -> feedService.getFeed("FOLLOW", 10, 0)
            );

            assertEquals(ErrorCode.USER_NOT_AUTHORIZED, ex.getErrorCode());
        }
    }

    @Test
    void getFeed_InvalidSort_ThrowsException() {
        ServiceException ex = assertThrows(
                ServiceException.class,
                () -> feedService.getFeed("INVALID", 10, 0)
        );

        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
    }

    @Test
    void getFeed_NewSortWithEmptyResult_ReturnsEmptyList() {
        when(feedRepository.getFeedByNew(anyInt(), anyInt()))
                .thenReturn(List.of());
        when(imageMapper.toResponse(anyList()))
                .thenReturn(List.of());

        GetImagesResponse response = feedService.getFeed("NEW", 10, 0);

        assertTrue(response.getImages().isEmpty());
    }

    @Test
    void getFeed_PopularSortWithMultipleLikes_ReturnsOrderedList() {
        Image mostPopular = createTestImage(3L);
        Image leastPopular = createTestImage(2L);

        when(feedRepository.getFeedByPopular(anyInt(), anyInt()))
                .thenReturn(List.of(mostPopular, leastPopular));

        GetImagesResponse response = feedService.getFeed("POPULAR", 10, 0);

        assertEquals(2, response.getImages().size());
    }

    @Test
    void getFeed_FollowSortWithNoSubscriptions_ReturnsEmptyList() {
        try (var utils = mockStatic(SecurityContextUtils.class)) {
            User user = createActiveUser();
            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(user);

            when(feedRepository.getFeedByFollow(anyLong(), anyInt(), anyInt()))
                    .thenReturn(List.of());

            GetImagesResponse response = feedService.getFeed("FOLLOW", 10, 0);

            assertTrue(response.getImages().isEmpty());
        }
    }

    @Test
    void getFeed_PaginationSecondPage_ReturnsLimitedResults() {
        when(feedRepository.getFeedByNew(10, 1))
                .thenReturn(List.of(createTestImage(1L)));
        when(imageMapper.toResponse(anyList()))
                .thenReturn(List.of(new ImageResponse()));

        GetImagesResponse response = feedService.getFeed("NEW", 10, 1);

        assertEquals(1, response.getImages().size());
    }

    @Test
    void getFeed_UserInactiveForFollowSort_ThrowsException() {
        try (var utils = mockStatic(SecurityContextUtils.class)) {
            User user = createInactiveUser();
            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(user);

            ServiceException ex = assertThrows(ServiceException.class,
                    () -> feedService.getFeed("FOLLOW", 10, 0));

            assertEquals(ErrorCode.CURRENT_USER_DELETED, ex.getErrorCode());
        }
    }

    @Test
    void getFeed_PrivateImagesInFollow_ExcludedFromResults() {
        try (var utils = mockStatic(SecurityContextUtils.class)) {
            User user = createActiveUser();
            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(user);

            Image publicImage = createTestImage(2L);
            Image privateImage = createTestImage(2L, Protection.PRIVATE);

            when(feedRepository.getFeedByFollow(anyLong(), anyInt(), anyInt()))
                    .thenReturn(List.of(publicImage, privateImage));

            GetImagesResponse response = feedService.getFeed("FOLLOW", 10, 0);

            assertEquals(2, response.getImages().size());
        }
    }

    private User createActiveUser() {
        return User.builder()
                .id(1L)
                .active(true)
                .nickname("testuser-" + UUID.randomUUID())
                .email(UUID.randomUUID() + "@example.com")
                .build();
    }

    private User createInactiveUser() {
        return User.builder()
                .id(2L)
                .active(false)
                .nickname("inactive-" + UUID.randomUUID())
                .email(UUID.randomUUID() + "@example.com")
                .build();
    }

    private Image createTestImage(Long ownerId) {
        return createTestImage(ownerId, Protection.PUBLIC);
    }

    private Image createTestImage(Long ownerId, Protection protection) {
        return Image.builder()
                .id(UUID.randomUUID())
                .owner(User.builder().id(ownerId).build())
                .protection(protection)
                .createdAt(LocalDateTime.now())
                .build();
    }
}