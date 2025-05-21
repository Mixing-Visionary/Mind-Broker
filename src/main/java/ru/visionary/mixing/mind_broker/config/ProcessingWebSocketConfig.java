package ru.visionary.mixing.mind_broker.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import ru.visionary.mixing.mind_broker.websocket.ProcessingHandshakeInterceptor;
import ru.visionary.mixing.mind_broker.websocket.ProcessingWebSocketHandler;

@Configuration
@RequiredArgsConstructor
public class ProcessingWebSocketConfig implements WebSocketConfigurer {
    private final ProcessingWebSocketHandler processingWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(processingWebSocketHandler, "/ws/processing")
                .setAllowedOrigins("*")
                .addInterceptors(new ProcessingHandshakeInterceptor());
    }
}
