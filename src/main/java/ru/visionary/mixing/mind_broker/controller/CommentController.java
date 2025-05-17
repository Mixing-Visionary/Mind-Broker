package ru.visionary.mixing.mind_broker.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.visionary.mixing.generated.api.CommentApi;
import ru.visionary.mixing.generated.model.GetCommentsResponse;
import ru.visionary.mixing.generated.model.SaveCommentRequest;
import ru.visionary.mixing.mind_broker.service.CommentService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CommentController implements CommentApi {
    private final CommentService commentService;

    @Override
    public ResponseEntity<Void> saveComment(UUID uuid, SaveCommentRequest request) {
        commentService.saveComment(uuid, request);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<GetCommentsResponse> getComments(UUID uuid, Integer size, Integer page) {
        return ResponseEntity.ok(commentService.getComments(uuid, size, page));
    }
}
