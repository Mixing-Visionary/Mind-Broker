package ru.visionary.mixing.mind_broker.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.visionary.mixing.generated.model.ImageResponse;
import ru.visionary.mixing.mind_broker.entity.Image;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ImageMapper {
    @Mapping(target = "uuid", source = "id")
    @Mapping(target = "authorId", source = "image.owner.id")
    @Mapping(target = "authorNickname", source = "image.owner.nickname")
    ImageResponse toResponse(Image image);

    List<ImageResponse> toResponse(List<Image> images);
}
