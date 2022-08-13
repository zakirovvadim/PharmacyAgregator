package ru.vadim.pharmacyagregator.repository.specification;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import ru.vadim.pharmacyagregator.domain.Pharm;
import ru.vadim.pharmacyagregator.domain.Pharm_;
import ru.vadim.pharmacyagregator.domain.PharmacyType_;
import ru.vadim.pharmacyagregator.domain.dto.filter.PharmFilter;

import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PharmSpec {
    public static Specification<Pharm> withFilter(PharmFilter filter, Pageable pageable) {
        return (root, query, builder) -> {
            final Collection<Predicate> predicates = new ArrayList<>();
            List<Order> orderList = new ArrayList();

            if (filter.getId() != null) {
                final Predicate pharmId = builder.equal(root.get(Pharm_.id), filter.getId());
                predicates.add(pharmId);
            }
            if (filter.getLink() != null) {
                final Predicate link = builder.equal(root.get(Pharm_.link), filter.getLink());
                predicates.add(link);
            }
            if (filter.getNumber() != null) {
                final Predicate type = builder.equal(root.get(Pharm_.number).get(PharmacyType_.NUMBER), filter.getNumber());
                predicates.add(type);
            }
            query.orderBy(orderList);
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
