package ru.vadim.pharmacyagregator.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.vadim.pharmacyagregator.util.AptekaRuParser;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AptekaRuController {
    private final AptekaRuParser parser;

    @GetMapping("/aptekaru")
    public void test() throws IOException {
        parser.parse("https://apteka.ru/?cityUrl=uchaly");
    }
}
