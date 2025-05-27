package ru.visionary.mixing.mind_broker.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.visionary.mixing.generated.api.LikeApi;
import ru.visionary.mixing.mind_broker.service.LikeService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class LikeController implements LikeApi {
    private final LikeService likeService;

    @Override
    public ResponseEntity<Void> like(UUID uuid) {
        likeService.likeImage(uuid);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> dislike(UUID uuid) {
        likeService.dislikeImage(uuid);
        return ResponseEntity.ok().build();
    }
}
