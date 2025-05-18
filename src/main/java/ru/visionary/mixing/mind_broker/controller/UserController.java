package ru.visionary.mixing.mind_broker.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.visionary.mixing.generated.api.UserApi;
import ru.visionary.mixing.generated.model.GetImagesResponse;
import ru.visionary.mixing.generated.model.UserResponse;
import ru.visionary.mixing.mind_broker.service.FollowService;
import ru.visionary.mixing.mind_broker.service.ImageService;
import ru.visionary.mixing.mind_broker.service.UserService;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {
    private final UserService userService;
    private final ImageService imageService;
    private final FollowService followService;

    @Override
    public ResponseEntity<UserResponse> getUser(Long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @Override
    public ResponseEntity<Void> updateUser(Long userId, String nickname, String description, String password, MultipartFile avatar) {
        userService.updateUser(userId, nickname, description, password, avatar);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> deleteUser(Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> deleteAvatar(Long userId) {
        userService.deleteAvatar(userId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<GetImagesResponse> getCurrentUserImages(Integer size, Integer page, String protection) {
        return ResponseEntity.ok(imageService.getImagesForCurrentUser(size, page, protection));
    }

    @Override
    public ResponseEntity<GetImagesResponse> getOtherUserImages(Long userId, Integer size, Integer page) {
        return ResponseEntity.ok(imageService.getImagesByUserId(userId, size, page));
    }

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
}
