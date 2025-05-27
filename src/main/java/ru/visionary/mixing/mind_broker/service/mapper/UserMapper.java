package ru.visionary.mixing.mind_broker.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.visionary.mixing.generated.model.UserResponse;
import ru.visionary.mixing.mind_broker.entity.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "userId", source = "id")
    @Mapping(target = "avatarUuid", source = "avatar")
    UserResponse toResponse(User user);
    List<UserResponse> toResponse(List<User> users);
}
