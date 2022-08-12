package ru.vadim.pharmacyagregator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.vadim.pharmacyagregator.domain.Pharm;

import java.util.List;

@Repository
public interface PharmRepo extends JpaRepository<Pharm, Long> {
    List<Pharm> findAllByIdIn(List<Long> ids);
}
