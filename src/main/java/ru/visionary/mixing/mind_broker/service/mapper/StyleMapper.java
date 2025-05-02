package ru.visionary.mixing.mind_broker.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.visionary.mixing.generated.model.StyleResponse;
import ru.visionary.mixing.mind_broker.entity.Style;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StyleMapper {
    @Mapping(target = "available", constant = "true")
    StyleResponse toResponse(Style style);

    List<StyleResponse> toResponse(List<Style> styles);
}
