package ru.vadim.pharmacyagregator.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Pharm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    Integer type;
}
