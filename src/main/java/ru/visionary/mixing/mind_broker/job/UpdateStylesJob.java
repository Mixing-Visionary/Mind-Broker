package ru.visionary.mixing.mind_broker.job;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.visionary.mixing.mind_broker.client.MegamindClient;
import ru.visionary.mixing.mind_broker.entity.Style;
import ru.visionary.mixing.mind_broker.repository.StyleRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateStylesJob {
    private final MegamindClient megamindClient;
    private final StyleRepository styleRepository;

    @PostConstruct
    private void initialUpdate() {
        updateStyles();
    }

    @Scheduled(cron = "${app.megamind.update-styles-job-cron}")
    public void updateStyles() {
        try {
            log.info("Start updating styles");

            List<String> actualStyles = megamindClient.getStyles().styles();
            if (actualStyles.isEmpty()) {
                log.warn("Received empty styles list, skipping update");
                return;
            }

            List<Style> currentStyles = styleRepository.findAll();

            List<String> currentStylesStr = currentStyles.stream().map(Style::name).toList();
            styleRepository.save(actualStyles.stream().filter(Predicate.not(currentStylesStr::contains)).toList());

            List<Integer> stylesToActivate = new ArrayList<>();
            List<Integer> stylesToDeactivate = new ArrayList<>();
            for (Style style : currentStyles) {
                boolean contains = actualStyles.contains(style.name());
                if (style.active() && !contains) {
                    stylesToDeactivate.add(style.id());
                } else if (!style.active() && contains) {
                    stylesToActivate.add(style.id());
                }
            }
            if (!stylesToActivate.isEmpty()) {
                styleRepository.updateActive(stylesToActivate, true);
            }
            if (!stylesToDeactivate.isEmpty()) {
                styleRepository.updateActive(stylesToDeactivate, false);
            }

            log.info("Styles successfully updated");
        } catch (Exception e) {
            log.error("Error on updating styles: {}", e.getMessage());
        }
    }
}
