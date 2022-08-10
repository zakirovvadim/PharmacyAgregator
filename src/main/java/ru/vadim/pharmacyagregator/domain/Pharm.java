package ru.vadim.pharmacyagregator.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.vadim.pharmacyagregator.domain.enums.PharmacyType;

import javax.persistence.*;
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
    @ManyToOne()
    @JoinColumn(name = "type_id", referencedColumnName = "id", updatable = false)
    PharmacyType typeId;
}
