package ru.visionary.mixing.mind_broker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties
@ConfigurationPropertiesScan
@EnableFeignClients
@EnableScheduling
public class MindBrokerApplication {
    public static void main(String[] args) {
        SpringApplication.run(MindBrokerApplication.class, args);
    }
}