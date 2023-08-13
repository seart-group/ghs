package ch.usi.si.seart.repository.criteria;

import ch.usi.si.seart.repository.operation.BinaryOperation;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;

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
            case LIKE -> {
                Assert.isInstanceOf(String.class, value, "Value must be a string for it to be used with LIKE");
                @SuppressWarnings("unchecked") Path<String> castKey = (Path<String>) key;
                String castValue = (String) value;
                yield criteriaBuilder.like(
                        criteriaBuilder.lower(castKey),
                        "%" + castValue.toLowerCase() + "%"
                );
            }
        };

    }
}
