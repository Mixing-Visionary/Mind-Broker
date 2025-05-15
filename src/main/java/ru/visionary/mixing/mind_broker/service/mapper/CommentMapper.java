package ru.visionary.mixing.mind_broker.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.visionary.mixing.generated.model.CommentResponse;
import ru.visionary.mixing.mind_broker.entity.Comment;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorNickname", source = "author.nickname")
    @Mapping(target = "authorAvatarUuid", source = "author.avatar")
    @Mapping(target = "authorActive", source = "author.active")
    @Mapping(target = "commentId", source = "comment.id")
    @Mapping(target = "createdAt", dateFormat = "dd.MM.yyyy HH:mm:ss")
    CommentResponse toResponse(Comment comment);

    List<CommentResponse> toResponse(List<Comment> comments);
}
