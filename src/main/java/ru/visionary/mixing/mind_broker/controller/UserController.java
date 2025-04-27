package ru.visionary.mixing.mind_broker.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.visionary.mixing.generated.api.UserApi;
import ru.visionary.mixing.mind_broker.service.UserService;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {
    private final UserService userService;

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
}
