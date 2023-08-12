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
        switch (operation) {
            case GREATER_THAN -> {
                return criteriaBuilder.greaterThan(key, value);
            }
            case GREATER_THAN_EQUAL -> {
                return criteriaBuilder.greaterThanOrEqualTo(key, value);
            }
            case LESS_THAN_EQUAL -> {
                return criteriaBuilder.lessThanOrEqualTo(key, value);
            }
            case EQUAL -> {
                return criteriaBuilder.equal(key, value);
            }
            case LIKE -> {
                Assert.isInstanceOf(String.class, value, "Value must be a string for it to be used with LIKE");
                @SuppressWarnings("unchecked") Path<String> castKey = (Path<String>) key;
                String castValue = (String) value;
                return criteriaBuilder.like(
                        criteriaBuilder.lower(castKey),
                        "%" + castValue.toLowerCase() + "%"
                );
            }
            default -> throw operation.toRuntimeException();
        }
    }
}
