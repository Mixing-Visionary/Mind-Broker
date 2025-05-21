package ru.visionary.mixing.mind_broker.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import ru.visionary.mixing.mind_broker.entity.ProcessingResultMessage;
import ru.visionary.mixing.mind_broker.entity.ProcessingStatus;
import ru.visionary.mixing.mind_broker.exception.ErrorCode;
import ru.visionary.mixing.mind_broker.exception.ServiceException;
import ru.visionary.mixing.mind_broker.service.ProcessingService;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProcessingWebSocketHandler extends TextWebSocketHandler {
    private final Map<UUID, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private final ProcessingService processingService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        UUID uuid = (UUID) session.getAttributes().get("uuid");
        log.info("WebSocket connection established - ID: {}", uuid);
        sessions.put(uuid, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.debug("Received WebSocket message - ID: {}, Payload: {}", session.getAttributes().get("uuid"), message.getPayload());
        if ("CANCEL".equals(message.getPayload())) {
            try {
                UUID uuid = (UUID) session.getAttributes().get("uuid");
                boolean canceled = processingService.cancelProcessing(uuid);
                if (canceled) {
                    log.info("Processing canceled successfully - ID: {}", uuid);
                    sendResult(
                            new ProcessingResultMessage(null, ProcessingStatus.CANCELED, null, null),
                            uuid,
                            true
                    );
                }
            } catch (Exception e) {
                throw new ServiceException(ErrorCode.INVALID_REQUEST);
            }
        }
    }

    public void sendResult(ProcessingResultMessage message, UUID uuid, boolean shouldClose) {
        log.debug("Attempting to send result - ID: {}, Status: {}", uuid, message.processingStatus());

        WebSocketSession session = sessions.get(uuid);
        if (session != null && session.isOpen()) {
            try {
                String json = mapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(json));
                if (shouldClose) {
                    sessions.remove(uuid).close();
                }
                log.debug("Result sent successfully - ID: {}", uuid);
            } catch (Exception e) {
                log.error("Failed to send WebSocket message - ID: {} | {}", uuid, e.getMessage(), e);
            }
        } else if (session != null) {
            log.warn("No active session found for ID: {}", uuid);
            sessions.remove(uuid);
        }
    }
}
