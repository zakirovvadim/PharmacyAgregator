package ru.vadim.pharmacyagregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.vadim.pharmacyagregator.domain.Pharm;
import ru.vadim.pharmacyagregator.repository.exception.NotFoundException;
import ru.vadim.pharmacyagregator.util.AptekaRuParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AptekaRuService {
    private final AptekaRuParser parser;
    private static final String PHARMACY_PATH = "https://apteka.ru/";
    private final KafkaTemplate<String, String> kafkaTemplate;

    //@Scheduled(cron = "0 0 0 * * *")
    public List<Pharm> launchAptekaRuParsing() {
        List<Pharm> parsingPharm = new ArrayList<>();
        try {
            parsingPharm.addAll(parser.parse(PHARMACY_PATH));
        } catch (NotFoundException | IOException | InterruptedException e) {
            log.error(String.format("Error with parsing from aptekaRu: %s", e.getMessage()));
        }
        return parsingPharm;
    }
}
