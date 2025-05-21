package ru.visionary.mixing.mind_broker.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.visionary.mixing.generated.model.ProcessingStatusResponse;
import ru.visionary.mixing.mind_broker.entity.Processing;

@Mapper(componentModel = "spring")
public interface ProcessingMapper {
    @Mapping(target = "base64Image", source = "result")
    @Mapping(target = "processingStatus", source = "status")
    ProcessingStatusResponse toResponse(Processing processing);
}
