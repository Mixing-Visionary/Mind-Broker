package ru.visionary.mixing.mind_broker.service.mapper;

import org.mapstruct.Mapper;
import ru.visionary.mixing.mind_broker.entity.User;
import ru.visionary.mixing.generated.model.RegisterRequest;

@Mapper(componentModel = "spring")
public interface AuthMapper {
    User requestToUser(RegisterRequest request);
}
