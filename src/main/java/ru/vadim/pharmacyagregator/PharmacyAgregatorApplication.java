package ru.vadim.pharmacyagregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class PharmacyAgregatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(PharmacyAgregatorApplication.class, args);
    }

}
