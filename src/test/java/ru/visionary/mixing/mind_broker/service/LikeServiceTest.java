package ru.visionary.mixing.mind_broker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.visionary.mixing.mind_broker.entity.Image;
import ru.visionary.mixing.mind_broker.entity.Protection;
import ru.visionary.mixing.mind_broker.entity.User;
import ru.visionary.mixing.mind_broker.repository.ImageRepository;
import ru.visionary.mixing.mind_broker.repository.LikeRepository;
import ru.visionary.mixing.mind_broker.utils.SecurityContextUtils;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LikeServiceTest {
    @Mock
    private ImageRepository imageRepository;
    @Mock
    private LikeRepository likeRepository;

    @InjectMocks
    private LikeService likeService;

    @Test
    void likeImage_ValidRequest_SavesLike() {
        try (MockedStatic<SecurityContextUtils> utils = mockStatic(SecurityContextUtils.class)) {
            UUID imageUuid = UUID.randomUUID();
            User user = User.builder().id(1L).active(true).build();
            Image image = Image.builder()
                    .id(imageUuid)
                    .owner(User.builder().active(true).id(2L).build())
                    .protection(Protection.PUBLIC)
                    .build();

            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(user);
            when(imageRepository.findById(imageUuid)).thenReturn(image);

            likeService.likeImage(imageUuid);

            verify(likeRepository).save(user.id(), imageUuid);
        }
    }
}
