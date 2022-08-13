package ru.vadim.pharmacyagregator.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import ru.vadim.pharmacyagregator.domain.Pharm;

import java.util.List;

@Repository
public interface PharmRepo extends PagingAndSortingRepository<Pharm, Long>, JpaSpecificationExecutor<Pharm> {
}
