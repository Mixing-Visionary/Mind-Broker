package ru.visionary.mixing.mind_broker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.visionary.mixing.generated.model.StylesResponse;
import ru.visionary.mixing.mind_broker.entity.Style;
import ru.visionary.mixing.mind_broker.repository.StyleRepository;
import ru.visionary.mixing.mind_broker.service.mapper.StyleMapper;
import ru.visionary.mixing.mind_broker.service.mapper.StyleMapperImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StyleServiceTest {
    @Mock
    private StyleRepository styleRepository;
    @Spy
    private StyleMapper styleMapper = new StyleMapperImpl();

    @InjectMocks
    private StyleService styleService;

    @Test
    void getStyles_ShouldReturnAllStylesFromRepository() {
        when(styleRepository.findAll()).thenReturn(List.of(
                new Style(1, "Style1", "icon1", true),
                new Style(2, "Style2", "icon2", false)
        ));

        StylesResponse response = styleService.getStyles();

        assertEquals(2, response.getStyles().size());
        assertEquals("Style1", response.getStyles().get(0).getName());
    }
}