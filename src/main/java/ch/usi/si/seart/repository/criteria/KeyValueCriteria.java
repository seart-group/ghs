package ch.usi.si.seart.repository.criteria;

import ch.usi.si.seart.repository.operation.BinaryOperation;
import org.jetbrains.annotations.NotNull;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public record KeyValueCriteria<E, T extends Comparable<T>>(
        Path<T> key, T value, BinaryOperation operation
) implements Criteria<E> {

    @Override
    public Predicate toPredicate(
            @NotNull Root<E> root, @NotNull CriteriaQuery<?> query, @NotNull CriteriaBuilder criteriaBuilder
    ) {
        return switch (operation) {
            case GREATER_THAN -> criteriaBuilder.greaterThan(key, value);
            case LESS_THAN -> criteriaBuilder.lessThan(key, value);
            case GREATER_THAN_EQUAL -> criteriaBuilder.greaterThanOrEqualTo(key, value);
            case LESS_THAN_EQUAL -> criteriaBuilder.lessThanOrEqualTo(key, value);
            case EQUAL -> criteriaBuilder.equal(key, value);
            case NOT_EQUAL -> criteriaBuilder.notEqual(key, value);
            case LIKE -> criteriaBuilder.like(
                    criteriaBuilder.lower(key.as(String.class)),
                    "%" + value.toString().toLowerCase() + "%"
            );
            case PREFIX -> criteriaBuilder.like(
                    criteriaBuilder.lower(key.as(String.class)),
                    value.toString().toLowerCase() + "%"
            );
            case SUFFIX -> criteriaBuilder.like(
                    criteriaBuilder.lower(key.as(String.class)),
                    "%" + value.toString().toLowerCase()
            );
        };
    }
}
