package ru.vadim.pharmacyagregator.domain.enums;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PharmacyType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    Integer number;
    String name;
}
