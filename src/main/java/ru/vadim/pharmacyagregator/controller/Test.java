package ru.vadim.pharmacyagregator.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.vadim.pharmacyagregator.service.ScladService;
import ru.vadim.pharmacyagregator.util.ParseSclad;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class Test {
    private final ScladService service;


    @GetMapping(value = "/test")
    public void registrationNative() throws IOException {
        service.launchScladParsingOrthopedic();
    }

}
