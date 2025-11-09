package com.example.userservice.configs;


import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Configuration
public class kafkaProducerConfig {

    private KafkaTemplate<String, String> kafkaTemplate;

    public kafkaProducerConfig(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void pubsilshEvent(String topic, String message) {
        kafkaTemplate.send(topic, message);
    }



}
