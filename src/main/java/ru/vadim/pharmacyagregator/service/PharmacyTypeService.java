package ru.vadim.pharmacyagregator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vadim.pharmacyagregator.domain.PharmacyType;
import ru.vadim.pharmacyagregator.repository.PharmacyTypeRepository;
import ru.vadim.pharmacyagregator.repository.exception.NotFoundException;

@Service
@RequiredArgsConstructor
public class PharmacyTypeService {

    private final PharmacyTypeRepository typeRepository;

    public PharmacyType findPharmacyTypeByNumber(Integer number) throws NotFoundException {
        return typeRepository.findByNumber(number).orElseThrow(() -> new NotFoundException(number));
    }

    public PharmacyType findById(Long id) throws NotFoundException {
        return typeRepository.findById(id).orElseThrow(() -> new NotFoundException(id));
    }
}
