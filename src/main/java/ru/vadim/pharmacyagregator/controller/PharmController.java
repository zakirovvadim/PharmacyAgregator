package ru.vadim.pharmacyagregator.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.vadim.pharmacyagregator.domain.Pharm;
import ru.vadim.pharmacyagregator.domain.dto.filter.PharmFilter;
import ru.vadim.pharmacyagregator.service.ScladService;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PharmController {
    private final ScladService service;

    @GetMapping( "/test")
    public void registrationNative() throws IOException {
        service.launchScladParsingPresentGoods();
    }

    @GetMapping("/pharmacies")
    public ResponseEntity<Page<Pharm>> getAllProducts(PharmFilter pharmFilter, Pageable pageable) {
        return new ResponseEntity<>(service.search(pharmFilter, pageable), HttpStatus.OK);
    }

    @PostMapping("/pharmacy")
    public Pharm savePharm(@RequestBody Pharm pharm) {
        return service.savePharm(pharm);
    }

    @DeleteMapping("pharmacy/{id}")
    public void deleteById(@PathVariable("id") Long id) {
        service.deleteData(id);
    }

}
