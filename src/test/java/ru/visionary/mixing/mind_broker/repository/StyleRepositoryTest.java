package ru.visionary.mixing.mind_broker.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.visionary.mixing.mind_broker.entity.Style;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class StyleRepositoryTest extends AbstractRepositoryTest {
    @Autowired
    private StyleRepository styleRepository;

    @Test
    void save_ShouldPersistNewStyle() {
        Style style = new Style(null, "New Style", null, true);

        styleRepository.save(style);

        Style saved = styleRepository.findByName("New Style");
        assertNotNull(saved);
        assertTrue(saved.active());
    }

    @Test
    void findAllActive_ShouldReturnOnlyActiveStyles() {
        styleRepository.save(List.of("Style1", "Style2"));

        List<Style> styles = styleRepository.findAll();
        assertEquals(2, styles.size());

        styleRepository.updateActive(List.of(styles.get(0).id()), false);

        List<Style> activeStyles = styleRepository.findAllActive();

        assertEquals(1, activeStyles.size());
        assertEquals(styles.get(1).name(), activeStyles.get(0).name());
    }

    @Test
    void updateActive_ShouldChangeMultipleStyles() {
        styleRepository.save(List.of("StyleA", "StyleB", "StyleC"));

        List<Style> styles = styleRepository.findAll();
        assertEquals(3, styles.size());

        styleRepository.updateActive(List.of(styles.get(0).id(), styles.get(1).id()), false);

        Style style1 = styleRepository.findByName(styles.get(0).name());
        Style style2 = styleRepository.findByName(styles.get(1).name());
        assertFalse(style1.active());
        assertFalse(style2.active());
    }
}