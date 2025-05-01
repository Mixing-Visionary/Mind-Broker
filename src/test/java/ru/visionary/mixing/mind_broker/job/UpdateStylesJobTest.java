package ru.visionary.mixing.mind_broker.job;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.visionary.mixing.mind_broker.client.MegamindClient;
import ru.visionary.mixing.mind_broker.client.dto.GetStylesResponse;
import ru.visionary.mixing.mind_broker.entity.Style;
import ru.visionary.mixing.mind_broker.repository.StyleRepository;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateStylesJobTest {
    @Mock
    private MegamindClient megamindClient;
    @Mock
    private StyleRepository styleRepository;

    @InjectMocks
    private UpdateStylesJob updateStylesJob;

    @Test
    void updateStyles_ShouldAddNewStylesAndUpdateExisting() {
        when(megamindClient.getStyles()).thenReturn(new GetStylesResponse(List.of("NewStyle")));
        when(styleRepository.findAll()).thenReturn(List.of(
                new Style(1, "OldStyle", null, true)
        ));

        updateStylesJob.updateStyles();

        verify(styleRepository).save(anyList());
        verify(styleRepository).updateActive(anyList(), eq(false));
    }
}