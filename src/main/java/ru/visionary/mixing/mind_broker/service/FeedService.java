package ru.visionary.mixing.mind_broker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.visionary.mixing.generated.model.GetImagesResponse;
import ru.visionary.mixing.generated.model.ImageResponse;
import ru.visionary.mixing.mind_broker.entity.FeedSortType;
import ru.visionary.mixing.mind_broker.entity.User;
import ru.visionary.mixing.mind_broker.exception.ErrorCode;
import ru.visionary.mixing.mind_broker.exception.ServiceException;
import ru.visionary.mixing.mind_broker.repository.FeedRepository;
import ru.visionary.mixing.mind_broker.service.mapper.ImageMapper;
import ru.visionary.mixing.mind_broker.utils.SecurityContextUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedService {
    private final FeedRepository feedRepository;
    private final ImageMapper imageMapper;

    public GetImagesResponse getFeed(String sort, Integer size, Integer page) {
        log.info("Processing feed request - Sort: {}, Size: {}, Page: {}", sort, size, page);

        FeedSortType feedSortType;
        try {
            feedSortType = FeedSortType.valueOf(sort.toUpperCase());
        } catch (Exception e) {
            log.warn("Invalid sort parameter: '{}'", sort, e);
            throw new ServiceException(ErrorCode.INVALID_REQUEST);
        }

        switch (feedSortType) {
            case NEW -> {
                log.debug("Processing NEW feed");
                return getFeedByNew(size, page);
            }
            case POPULAR -> {
                log.debug("Processing POPULAR feed");
                return getFeedByPopular(size, page);
            }
            case FOLLOW -> {
                log.debug("Processing FOLLOW feed");
                return getFeedByFollow(size, page);
            }
            default -> {
                log.error("Unhandled feed sort type: {}", feedSortType);
                throw new ServiceException(ErrorCode.INVALID_REQUEST);
            }
        }
    }

    private GetImagesResponse getFeedByNew(Integer size, Integer page) {
        log.debug("Fetching NEW feed - Size: {}, Page: {}", size, page);

        User user = SecurityContextUtils.getAuthenticatedUser();

        List<ImageResponse> images = imageMapper.toResponse(feedRepository.getFeedByNew(user == null ? null : user.id(), size, page));
        log.info("Fetched {} NEW images", images.size());

        return new GetImagesResponse(images);
    }

    private GetImagesResponse getFeedByPopular(Integer size, Integer page) {
        log.debug("Fetching POPULAR feed - Size: {}, Page: {}", size, page);

        User user = SecurityContextUtils.getAuthenticatedUser();

        List<ImageResponse> images = imageMapper.toResponse(feedRepository.getFeedByPopular(user == null ? null : user.id(), size, page));
        log.info("Fetched {} POPULAR images", images.size());

        return new GetImagesResponse(images);
    }

    private GetImagesResponse getFeedByFollow(Integer size, Integer page) {
        log.debug("Fetching FOLLOW feed - Size: {}, Page: {}", size, page);

        User user = SecurityContextUtils.getAuthenticatedUser();
        if (user == null) {
            log.warn("Unauthorized attempt to access FOLLOW feed");
            throw new ServiceException(ErrorCode.USER_NOT_AUTHORIZED);
        }
        if (!user.active()) {
            log.warn("Deleted user attempt to access FOLLOW feed: {}", user.id());
            throw new ServiceException(ErrorCode.CURRENT_USER_DELETED);
        }

        log.debug("Fetching FOLLOW feed for user {}", user.id());

        List<ImageResponse> images = imageMapper.toResponse(feedRepository.getFeedByFollow(user.id(), size, page));
        log.info("Fetched {} FOLLOW images for user {}", images.size(), user.id());

        return new GetImagesResponse(images);
    }
}
