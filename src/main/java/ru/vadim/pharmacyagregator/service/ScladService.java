package ru.vadim.pharmacyagregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.vadim.pharmacyagregator.domain.Pharm;
import ru.vadim.pharmacyagregator.repository.PharmRepo;
import ru.vadim.pharmacyagregator.util.ParseSclad;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScladService {
    private final ParseSclad parseSclad;
    private final PharmRepo pharmRepo;

    //@Scheduled(cron = "0 0 0 * * *")
    public void launchScladParsing() {
        String pilullsLink = "https://apteka74.ru/catalog/__179/";
        try {
            List<Pharm> parsedPharmacies = parseSclad.parse(pilullsLink);
            updateScheduleData(parsedPharmacies);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    //@Scheduled(cron = "0 0 0 * * *")
    public void launchScladParsingBads() {
        String badsLink = "https://apteka74.ru/catalog/__388/";
        try {
            List<Pharm> parsedPharmacies = parseSclad.parse(badsLink);
            updateScheduleData(parsedPharmacies);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    //@Scheduled(cron = "0 0 0 * * *")
    public void launchScladParsingEquipment() {
        String badsLink = "https://apteka74.ru/catalog/__526/";
        try {
            List<Pharm> parsedPharmacies = parseSclad.parse(badsLink);
            updateScheduleData(parsedPharmacies);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    //@Scheduled(cron = "0 0 0 * * *")
    public void launchScladParsingHygiene() {
        String badsLink = "https://apteka74.ru/catalog/__472/";
        try {
            List<Pharm> parsedPharmacies = parseSclad.parse(badsLink);
            updateScheduleData(parsedPharmacies);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    //@Scheduled(cron = "0 0 0 * * *")
    public void launchScladParsingDiet() {
        String badsLink = "https://apteka74.ru/catalog/__631/";
        try {
            List<Pharm> parsedPharmacies = parseSclad.parse(badsLink);
            updateScheduleData(parsedPharmacies);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    //@Scheduled(cron = "0 0 0 * * *")
    public void launchScladParsingMotherCare() {
        String badsLink = "https://apteka74.ru/catalog/__574/";
        try {
            List<Pharm> parsedPharmacies = parseSclad.parse(badsLink);
            updateScheduleData(parsedPharmacies);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    //@Scheduled(cron = "0 0 0 * * *")
    public void launchScladParsingOrthopedic() {
        String badsLink = "https://apteka74.ru/catalog/__677/";
        try {
            List<Pharm> parsedPharmacies = parseSclad.parse(badsLink);
            updateScheduleData(parsedPharmacies);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    //@Scheduled(cron = "0 0 0 * * *")
    public void launchScladParsingPresentGoods() {
        String badsLink = "https://apteka74.ru/catalog/__716/";
        try {
            List<Pharm> parsedPharmacies = parseSclad.parse(badsLink);
            updateScheduleData(parsedPharmacies);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    //@Scheduled(cron = "0 0 0 * * *")
    public void launchScladParsingVeterinary() {
        String badsLink = "https://apteka74.ru/catalog/__666/";
        try {
            List<Pharm> parsedPharmacies = parseSclad.parse(badsLink);
            updateScheduleData(parsedPharmacies);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void updateScheduleData(List<Pharm> parsedPharmacies) {
        Map<Long, Pharm> existedPharm = pharmRepo.findAllById(parsedPharmacies.stream().map(Pharm::getId).collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(Pharm::getId, Function.identity()));
        List<Pharm> toUpdate = parsedPharmacies.stream().filter(newElement -> newElement.equals(existedPharm.get(newElement.getId()))).toList();
        pharmRepo.saveAll(toUpdate.isEmpty() ? parsedPharmacies : toUpdate);
    }

    private Pharm update(Pharm pharm) {
        return saveData(pharm);
    }

    private void deleteData(Long id) {
        pharmRepo.deleteById(id);
    }

    private Pharm saveData(Pharm pharmacy) {
        return pharmRepo.save(pharmacy);
    }
}
