package ru.vadim.pharmacyagregator.domain.dto.filter;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import ru.vadim.pharmacyagregator.domain.PharmacyType;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PharmFilter implements Serializable {
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
    Long number;

    public void resetFields() {
        this.id = null;
        this.delivery = null;
        this.title = null;
        this.expirationDate = null;
        this.producerPharm = null;
        this.activeSubstance = null;
        this.explanation = null;
        this.price = null;
        this.oldPrice = null;
        this.count = null;
        this.link = null;
        this.number = null;
    }
}
