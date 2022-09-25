package ru.vadim.pharmacyagregator.configuration.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaLiseners {
    @KafkaListener(topics = "vadim", groupId = "groupId")
    void listener(String data) {
        System.out.println("listener receivd: " + data);
    }
}
