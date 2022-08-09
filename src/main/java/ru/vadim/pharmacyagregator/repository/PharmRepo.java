package ru.vadim.pharmacyagregator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.vadim.pharmacyagregator.domain.Pharm;

@Repository
public interface PharmRepo extends JpaRepository<Pharm, Long> {
}
