package ru.visionary.mixing.mind_broker.service;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.stereotype.Service;
import ru.visionary.mixing.mind_broker.config.properties.RabbitProperties;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RabbitService {
    private final RabbitProperties properties;
    private final AmqpAdmin amqpAdmin;
    private final DirectExchange processedExchange;

    public void createResponseQueue(UUID uuid) {
        Queue queue = QueueBuilder
                .nonDurable(properties.processedQueuePrefix() + uuid.toString())
                .expires(properties.processedQueueExpires())
                .autoDelete()
                .build();

        amqpAdmin.declareQueue(queue);
        amqpAdmin.declareBinding(BindingBuilder
                .bind(queue)
                .to(processedExchange)
                .with(uuid.toString()));
    }
}
