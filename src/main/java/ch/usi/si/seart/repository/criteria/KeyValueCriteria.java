package ch.usi.si.seart.repository.criteria;

import ch.usi.si.seart.repository.operation.BinaryOperation;
import org.jetbrains.annotations.NotNull;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

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
        };
    }
}
