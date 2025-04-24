package ru.visionary.mixing.mind_broker.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.visionary.mixing.generated.model.RegisterRequest;
import ru.visionary.mixing.mind_broker.entity.User;

@Mapper(componentModel = "spring")
public abstract class AuthMapper {
    private PasswordEncoder passwordEncoder;

    @Mapping(target = "password", qualifiedByName = "password")
    public abstract User requestToUser(RegisterRequest request);

    @Named("password")
    protected String convertPassword(String password) {
        return passwordEncoder.encode(password);
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
}
