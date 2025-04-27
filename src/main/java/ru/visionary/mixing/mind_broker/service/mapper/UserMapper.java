package ru.visionary.mixing.mind_broker.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import ru.visionary.mixing.generated.model.UserResponse;
import ru.visionary.mixing.mind_broker.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mappings({
            @Mapping(target = "userId", source = "id"),
            @Mapping(target = "avatarUuid", source = "avatar")
    })
    UserResponse toResponse(User user);
}
