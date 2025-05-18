package ru.visionary.mixing.mind_broker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.visionary.mixing.generated.model.CommentResponse;
import ru.visionary.mixing.generated.model.GetCommentsResponse;
import ru.visionary.mixing.generated.model.SaveCommentRequest;
import ru.visionary.mixing.mind_broker.entity.Comment;
import ru.visionary.mixing.mind_broker.entity.Image;
import ru.visionary.mixing.mind_broker.entity.User;
import ru.visionary.mixing.mind_broker.exception.ServiceException;
import ru.visionary.mixing.mind_broker.repository.CommentRepository;
import ru.visionary.mixing.mind_broker.repository.ImageRepository;
import ru.visionary.mixing.mind_broker.service.mapper.CommentMapper;
import ru.visionary.mixing.mind_broker.utils.SecurityContextUtils;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ImageRepository imageRepository;
    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentService commentService;

    @Test
    void saveComment_UnauthorizedUser_ThrowsException() {
        try (MockedStatic<SecurityContextUtils> utils = mockStatic(SecurityContextUtils.class)) {
            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(null);

            assertThrows(ServiceException.class,
                    () -> commentService.saveComment(UUID.randomUUID(), new SaveCommentRequest().comment("test")));
        }
    }

    @Test
    void saveComment_ImageNotFound_ThrowsException() {
        try (MockedStatic<SecurityContextUtils> utils = mockStatic(SecurityContextUtils.class)) {
            User user = User.builder().id(1L).active(true).build();
            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(user);

            when(imageRepository.findById(any(UUID.class))).thenReturn(null);

            assertThrows(ServiceException.class,
                    () -> commentService.saveComment(UUID.randomUUID(), new SaveCommentRequest()));
        }
    }

    @Test
    void getComments_ValidRequest_ReturnsResponse() {
        try (MockedStatic<SecurityContextUtils> utils = mockStatic(SecurityContextUtils.class)) {
            UUID imageUuid = UUID.randomUUID();
            User user = User.builder().id(1L).active(true).build();
            Image image = Image.builder().id(imageUuid).owner(user).build();
            Comment comment = Comment.builder().id(1L).comment("test").build();
            CommentResponse commentResponse = new CommentResponse();

            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(user);
            when(imageRepository.findById(imageUuid)).thenReturn(image);
            when(commentRepository.findByImage(imageUuid, 10, 0)).thenReturn(List.of(comment));
            when(commentMapper.toResponse(List.of(comment))).thenReturn(List.of(commentResponse));

            GetCommentsResponse result = commentService.getComments(imageUuid, 10, 0);

            assertNotNull(result);
            assertEquals(1, result.getComments().size());
        }
    }
}