package ru.visionary.mixing.mind_broker.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.visionary.mixing.mind_broker.config.properties.ProcessingProperties;
import ru.visionary.mixing.mind_broker.entity.Processing;
import ru.visionary.mixing.mind_broker.entity.ProcessingResultMessage;
import ru.visionary.mixing.mind_broker.entity.ProcessingStatus;
import ru.visionary.mixing.mind_broker.repository.ProcessingRepository;
import ru.visionary.mixing.mind_broker.websocket.ProcessingWebSocketHandler;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CancelLongProcessingJob {
    private final ProcessingRepository processingRepository;
    private final ProcessingProperties processingProperties;
    private final ProcessingWebSocketHandler processingWebSocketHandler;

    @Scheduled(cron = "${app.processing.cancel-long-processing-job-cron}")
    public void cancelLongProcessingJob() {
        LocalDateTime oldDate = LocalDateTime.now().minusMinutes(processingProperties.maxProcessingTimeMinutes());
        List<Processing> canceled = processingRepository.cancelLongProcessing(oldDate);
        canceled.forEach(processing -> processingWebSocketHandler.sendResult(
                new ProcessingResultMessage(null, ProcessingStatus.CANCELED, null, null),
                processing.id(),
                true
        ));
    }
}
