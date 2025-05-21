package ru.visionary.mixing.mind_broker.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.visionary.mixing.mind_broker.config.properties.ProcessingProperties;
import ru.visionary.mixing.mind_broker.repository.ProcessingRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClearOldResultJob {
    private final ProcessingRepository processingRepository;
    private final ProcessingProperties processingProperties;

    @Scheduled(cron = "${app.processing.clear-old-result-job-cron}")
    public void clearOldResults() {
        LocalDateTime oldDate = LocalDateTime.now().minusMinutes(processingProperties.resultTtlMinutes());
        processingRepository.clearResultWhereLoadedBefore(oldDate);
    }
}
