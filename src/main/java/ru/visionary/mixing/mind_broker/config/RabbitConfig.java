package ru.visionary.mixing.mind_broker.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.visionary.mixing.mind_broker.config.properties.RabbitProperties;

@Configuration
@RequiredArgsConstructor
public class RabbitConfig {
    private final RabbitProperties properties;

    @Bean
    public FanoutExchange processingExchange() {
        return new FanoutExchange(properties.processingExchange());
    }

    @Bean
    public Queue processingQueue() {
        return new Queue(properties.processingQueue());
    }

    @Bean
    public Binding processingBinding(Queue processingQueue, FanoutExchange processingExchange) {
        return BindingBuilder.bind(processingQueue).to(processingExchange);
    }

    @Bean
    public DirectExchange processedExchange() {
        return new DirectExchange(properties.processedExchange());
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter
    ) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
