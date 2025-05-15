package ru.visionary.mixing.mind_broker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.visionary.mixing.generated.model.CommentResponse;
import ru.visionary.mixing.generated.model.GetCommentsResponse;
import ru.visionary.mixing.generated.model.SaveCommentRequest;
import ru.visionary.mixing.mind_broker.entity.Comment;
import ru.visionary.mixing.mind_broker.entity.Image;
import ru.visionary.mixing.mind_broker.entity.Protection;
import ru.visionary.mixing.mind_broker.entity.User;
import ru.visionary.mixing.mind_broker.exception.ErrorCode;
import ru.visionary.mixing.mind_broker.exception.ServiceException;
import ru.visionary.mixing.mind_broker.repository.CommentRepository;
import ru.visionary.mixing.mind_broker.repository.ImageRepository;
import ru.visionary.mixing.mind_broker.service.mapper.CommentMapper;
import ru.visionary.mixing.mind_broker.utils.SecurityContextUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {
    private final CommentRepository commentRepository;
    private final ImageRepository imageRepository;
    private final CommentMapper commentMapper;

    public void saveComment(UUID imageUuid, SaveCommentRequest request) {
        User user = SecurityContextUtils.getAuthenticatedUser();
        if (user == null) {
            log.warn("Attempt to save comment by unauthorized user for image: {}", imageUuid);
            throw new ServiceException(ErrorCode.USER_NOT_AUTHORIZED);
        }
        if (!user.active()) {
            log.warn("Comment attempt by deleted user {} for image: {}", user.id(), imageUuid);
            throw new ServiceException(ErrorCode.CURRENT_USER_DELETED);
        }

        log.info("User {} trying to comment image {}", user.id(), imageUuid);

        Image image = imageRepository.findById(imageUuid);
        if (image == null) {
            log.error("Comment failed - image not found: {}", imageUuid);
            throw new ServiceException(ErrorCode.IMAGE_NOT_FOUND);
        }
        if (Protection.PRIVATE.equals(image.protection())) {
            log.warn("Attempt to comment private image {} by user {}", imageUuid, user.id());
            throw new ServiceException(ErrorCode.ACCESS_FORBIDDEN);
        }
        if (!image.owner().active()) {
            log.warn("Comment failed - image owner {} is deleted", image.owner().id());
            throw new ServiceException(ErrorCode.OWNER_DELETED);
        }

        commentRepository.save(Comment.builder()
                .author(user)
                .image(image)
                .comment(request.getComment())
                .build());

        log.info("Comment successfully added to image {} by user {}", imageUuid, user.id());
    }

    public GetCommentsResponse getComments(UUID imageUuid, int size, int page) {
        log.info("Fetching comments for image {} (size: {}, page: {})", imageUuid, size, page);

        Image image = imageRepository.findById(imageUuid);
        if (image == null) {
            log.error("Comments request failed - image not found: {}", imageUuid);
            throw new ServiceException(ErrorCode.IMAGE_NOT_FOUND);
        }
        if (Protection.PRIVATE.equals(image.protection())) {
            log.warn("Attempt to access comments for private image: {}", imageUuid);
            throw new ServiceException(ErrorCode.ACCESS_FORBIDDEN);
        }
        if (!image.owner().active()) {
            log.warn("Comments request failed - image owner {} is deleted", image.owner().id());
            throw new ServiceException(ErrorCode.OWNER_DELETED);
        }

        List<CommentResponse> comments = commentMapper.toResponse(commentRepository.findByImage(imageUuid, size, page));

        log.debug("Successfully fetched {} comments for image {}", comments.size(), imageUuid);

        return new GetCommentsResponse().comments(comments);
    }
}
