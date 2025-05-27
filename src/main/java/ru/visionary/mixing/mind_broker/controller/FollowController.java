package ru.visionary.mixing.mind_broker.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.visionary.mixing.generated.api.FollowApi;
import ru.visionary.mixing.generated.model.UsersResponse;
import ru.visionary.mixing.mind_broker.service.FollowService;

@RestController
@RequiredArgsConstructor
public class FollowController implements FollowApi {
    private final FollowService followService;

    @Override
    public ResponseEntity<Void> follow(Long userId) {
        followService.follow(userId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> unfollow(Long userId) {
        followService.unfollow(userId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<UsersResponse> getCurrentFollowers(Integer size, Integer page) {
        return ResponseEntity.ok(followService.getCurrentFollowers(size, page));
    }

    @Override
    public ResponseEntity<UsersResponse> getCurrentFollows(Integer size, Integer page) {
        return ResponseEntity.ok(followService.getCurrentFollows(size, page));
    }

    @Override
    public ResponseEntity<UsersResponse> getFollowers(Long userId, Integer size, Integer page) {
        return ResponseEntity.ok(followService.getFollowers(userId, size, page));
    }

    @Override
    public ResponseEntity<UsersResponse> getFollows(Long userId, Integer size, Integer page) {
        return ResponseEntity.ok(followService.getFollows(userId, size, page));
    }
}
