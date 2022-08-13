package ru.vadim.pharmacyagregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.vadim.pharmacyagregator.domain.Pharm;
import ru.vadim.pharmacyagregator.domain.dto.filter.PharmFilter;
import ru.vadim.pharmacyagregator.repository.PharmRepo;
import ru.vadim.pharmacyagregator.repository.specification.PharmSpec;
import ru.vadim.pharmacyagregator.util.ParseSclad;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScladService {
    private final ParseSclad parseSclad;
    private final PharmRepo pharmRepo;

    public Page<Pharm> search(PharmFilter pharmFilter, Pageable pageable) {
        Specification<Pharm> specification = PharmSpec.withFilter(pharmFilter, pageable);
        return pharmRepo.findAll(specification, pageable);
    }

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
        pharmRepo.saveAll(parsedPharmacies);
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
