package ru.visionary.mixing.mind_broker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
@ConfigurationPropertiesScan
public class MindBrokerApplication {
    public static void main(String[] args) {
        SpringApplication.run(MindBrokerApplication.class, args);
    }
}