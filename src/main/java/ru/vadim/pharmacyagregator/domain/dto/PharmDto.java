package ru.vadim.pharmacyagregator.domain.dto;

import lombok.Data;
import ru.vadim.pharmacyagregator.domain.Pharm;

import java.time.LocalDate;

@Data
public class PharmDto {
    Long id;
    Boolean delivery;
    String title;
    LocalDate expirationDate;
    String producerPharm;
    String activeSubstance;
    String explanation;
    Double price;
    Double oldPrice;
    Integer count;
    String link;
    Integer number;

    public PharmDto(Pharm pharm) {
        this.id = pharm.getId();
        this.delivery = pharm.getDelivery();
        this.title = pharm.getTitle();
        this.expirationDate = pharm.getExpirationDate();
        this.producerPharm = pharm.getProducerPharm();
        this.activeSubstance = pharm.getActiveSubstance();
        this.explanation = pharm.getExplanation();
        this.price = pharm.getPrice();
        this.oldPrice = pharm.getOldPrice();
        this.count = pharm.getCount();
        this.link = pharm.getLink();
        this.number = pharm.getNumber().getNumber();
    }
}
