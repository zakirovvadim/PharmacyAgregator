package ru.vadim.pharmacyagregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class PharmacyAgregatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(PharmacyAgregatorApplication.class, args);
    }

}
