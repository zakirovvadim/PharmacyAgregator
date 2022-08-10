package ru.vadim.pharmacyagregator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.vadim.pharmacyagregator.domain.enums.PharmacyType;

import java.util.Optional;

@Repository
public interface PharmacyTypeRepository extends JpaRepository<PharmacyType, Long> {
    Optional<PharmacyType> findByNumber(Integer number);
}
