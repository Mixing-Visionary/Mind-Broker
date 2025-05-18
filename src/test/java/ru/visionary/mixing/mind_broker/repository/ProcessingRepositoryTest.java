package ru.visionary.mixing.mind_broker.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.visionary.mixing.mind_broker.entity.Processing;
import ru.visionary.mixing.mind_broker.entity.ProcessingStatus;
import ru.visionary.mixing.mind_broker.entity.Style;
import ru.visionary.mixing.mind_broker.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProcessingRepositoryTest extends AbstractRepositoryTest {
    @Autowired
    private ProcessingRepository processingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StyleRepository styleRepository;

    private User testUser;
    private Long userId;
    private Style testStyle;
    private UUID processingId;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .nickname("test_user")
                .email("test@example.com")
                .password("password")
                .active(true)
                .build();
        userId = userRepository.save(testUser);

        testStyle = Style.builder()
                .name("Test Style")
                .active(true)
                .build();
        styleRepository.save(testStyle);
        testStyle = styleRepository.findByName("Test Style");

        processingId = UUID.randomUUID();
        Processing processing = Processing.builder()
                .id(processingId)
                .user(User.builder().id(userId).build())
                .style(testStyle)
                .startTime(LocalDateTime.now())
                .status(ProcessingStatus.PENDING)
                .statusAt(LocalDateTime.now())
                .build();
        processingRepository.save(processing);
    }

    @Test
    void findById_ShouldReturnFullProcessingEntity() {
        Processing result = processingRepository.findById(processingId);

        assertNotNull(result);
        assertEquals(processingId, result.id());
        assertEquals(ProcessingStatus.PENDING, result.status());

        assertNotNull(result.style());
        assertEquals(testStyle.id(), result.style().id());
        assertEquals(testStyle.name(), result.style().name());

        assertNotNull(result.startTime());
        assertNotNull(result.statusAt());
    }

    @Test
    void findById_ShouldReturnNullForNonExistentId() {
        UUID nonExistentId = UUID.randomUUID();

        Processing result = processingRepository.findById(nonExistentId);

        assertNull(result);
    }

    @Test
    void save_ShouldPersistAllFieldsCorrectly() {
        UUID newId = UUID.randomUUID();
        Processing newProcessing = Processing.builder()
                .id(newId)
                .user(User.builder().id(userId).build())
                .style(testStyle)
                .build();

        processingRepository.save(newProcessing);
        Processing result = processingRepository.findById(newId);

        assertNotNull(result);
        assertEquals(newId, result.id());
        assertEquals(testStyle.id(), result.style().id());
    }

    @Test
    void updateStatus_ShouldChangeStatusAndUpdateTimestamp() {
        LocalDateTime initialStatusAt = processingRepository.findById(processingId).statusAt();

        processingRepository.updateStatus(processingId, ProcessingStatus.COMPLETED);
        Processing updated = processingRepository.findById(processingId);

        assertEquals(ProcessingStatus.COMPLETED, updated.status());
        assertTrue(updated.statusAt().isAfter(initialStatusAt));
    }

    @Test
    void getStartTimeById_ShouldReturnCorrectTimestamp() {
        Processing processing = processingRepository.findById(processingId);

        LocalDateTime startTime = processingRepository.getStartTimeById(processingId);

        assertEquals(processing.startTime(), startTime);
    }

    @Test
    void cancelPending_WithPendingStatus_ShouldCancel() {
        int affected = processingRepository.cancelPending(processingId);

        assertEquals(1, affected);
        Processing updated = processingRepository.findById(processingId);
        assertEquals(ProcessingStatus.CANCELED, updated.status());
    }

    @Test
    void cancelLongProcessing_ShouldNotAffectRecentProcessing() {
        processingRepository.updateStatus(processingId, ProcessingStatus.PROCESSING);

        List<Processing> canceled = processingRepository.cancelLongProcessing(
                LocalDateTime.now().minusMinutes(5)
        );

        assertTrue(canceled.isEmpty());
        Processing notUpdated = processingRepository.findById(processingId);
        assertEquals(ProcessingStatus.PROCESSING, notUpdated.status());
    }
}