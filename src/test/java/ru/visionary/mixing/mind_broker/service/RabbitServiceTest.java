package ru.visionary.mixing.mind_broker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import ru.visionary.mixing.mind_broker.config.properties.RabbitProperties;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RabbitServiceTest {
    @Mock
    private AmqpAdmin amqpAdmin;
    @Mock
    private RabbitProperties rabbitProperties;
    @Mock
    private DirectExchange processedExchange;

    @InjectMocks
    private RabbitService rabbitService;

    @Test
    void createResponseQueue_ShouldDeclareQueueAndBinding() {
        UUID uuid = UUID.randomUUID();
        when(rabbitProperties.processedQueuePrefix()).thenReturn("processed-");
        when(rabbitProperties.processedQueueExpires()).thenReturn(1800000);

        rabbitService.createResponseQueue(uuid);

        verify(amqpAdmin).declareQueue(any(Queue.class));
        verify(amqpAdmin).declareBinding(any(Binding.class));
    }
}