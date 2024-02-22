package ch.usi.si.seart.repository.criteria;

import ch.usi.si.seart.repository.operation.TernaryOperation;
import org.jetbrains.annotations.NotNull;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public record KeyDualValueCriteria<E, T extends Comparable<T>>(
        Path<T> key, T first, T second, TernaryOperation operation
) implements Criteria<E> {

    @Override
    public Predicate toPredicate(
            @NotNull Root<E> root, @NotNull CriteriaQuery<?> query, @NotNull CriteriaBuilder criteriaBuilder
    ) {
        return switch (operation) {
            case BETWEEN -> criteriaBuilder.between(key, first, second);
        };
    }
}
