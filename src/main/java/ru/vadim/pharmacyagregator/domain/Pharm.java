package ru.vadim.pharmacyagregator.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.time.LocalDate;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Pharm {
    @Id
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
    @ManyToOne()
    @JoinColumn(name = "type_id", referencedColumnName = "id", updatable = false)
    PharmacyType number;
}
