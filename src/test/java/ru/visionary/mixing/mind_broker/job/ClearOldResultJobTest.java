package ru.visionary.mixing.mind_broker.job;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.visionary.mixing.mind_broker.config.properties.ProcessingProperties;
import ru.visionary.mixing.mind_broker.repository.ProcessingRepository;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClearOldResultJobTest {

    @Mock
    private ProcessingRepository processingRepository;

    @Mock
    private ProcessingProperties processingProperties;

    @InjectMocks
    private ClearOldResultJob clearOldResultJob;

    @Test
    void clearOldResults_ShouldClearDataOlderThanTTL() {
        int ttlMinutes = 15;
        LocalDateTime testTime = LocalDateTime.now().minusMinutes(ttlMinutes);

        when(processingProperties.resultTtlMinutes()).thenReturn(ttlMinutes);

        clearOldResultJob.clearOldResults();

        verify(processingRepository).clearResultWhereLoadedBefore(any(LocalDateTime.class));
        verify(processingProperties).resultTtlMinutes();
    }

    @Test
    void clearOldResults_ShouldUseCorrectCutoffTime() {
        int ttlMinutes = 30;
        LocalDateTime expectedTime = LocalDateTime.now().minusMinutes(ttlMinutes);

        when(processingProperties.resultTtlMinutes()).thenReturn(ttlMinutes);

        clearOldResultJob.clearOldResults();

        verify(processingRepository).clearResultWhereLoadedBefore(
                argThat(actualTime ->
                        actualTime.isAfter(expectedTime.minusSeconds(1)) &&
                                actualTime.isBefore(expectedTime.plusSeconds(1))
                )
        );
    }

    @Test
    void clearOldResults_NoDataToClear_ShouldCompleteNormally() {
        when(processingProperties.resultTtlMinutes()).thenReturn(60);
        doNothing().when(processingRepository).clearResultWhereLoadedBefore(any());

        clearOldResultJob.clearOldResults();

        verify(processingRepository).clearResultWhereLoadedBefore(any(LocalDateTime.class));
    }

    @Test
    void clearOldResults_VerifyTTLCalculation() {
        int ttlMinutes = 45;
        LocalDateTime expected = LocalDateTime.now().minusMinutes(ttlMinutes);

        when(processingProperties.resultTtlMinutes()).thenReturn(ttlMinutes);

        clearOldResultJob.clearOldResults();

        verify(processingRepository).clearResultWhereLoadedBefore(
                argThat(actual ->
                        actual.isAfter(expected.minusSeconds(2)) &&
                                actual.isBefore(expected.plusSeconds(2))
                )
        );
    }
}