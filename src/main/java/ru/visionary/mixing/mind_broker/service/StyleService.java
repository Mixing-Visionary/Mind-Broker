package ru.visionary.mixing.mind_broker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.visionary.mixing.generated.model.StylesResponse;
import ru.visionary.mixing.mind_broker.entity.Style;
import ru.visionary.mixing.mind_broker.repository.StyleRepository;
import ru.visionary.mixing.mind_broker.service.mapper.StyleMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StyleService {
    private final StyleRepository styleRepository;
    private final StyleMapper styleMapper;

    public StylesResponse getStyles() {
        List<Style> styles = styleRepository.findAll();
        return new StylesResponse(styleMapper.toResponse(styles));
    }
}
