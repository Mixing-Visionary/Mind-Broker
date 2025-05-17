package ru.visionary.mixing.mind_broker.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.visionary.mixing.generated.api.FeedApi;
import ru.visionary.mixing.generated.model.GetImagesResponse;
import ru.visionary.mixing.mind_broker.service.FeedService;

@RestController
@RequiredArgsConstructor
public class FeedController implements FeedApi {
    private final FeedService feedService;

    @Override
    public ResponseEntity<GetImagesResponse> getFeed(String sort, Integer size, Integer page) {
        return ResponseEntity.ok(feedService.getFeed(sort, size, page));
    }
}
