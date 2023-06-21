package usi.si.seart.repository.criteria;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;
import usi.si.seart.repository.operation.BinaryOperation;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KeyValueCriteria<E, T extends Comparable<T>> implements Criteria<E> {

    Path<T> key;
    T value;
    BinaryOperation operation;

    @Override
    public Predicate toPredicate(
            @NotNull Root<E> root, @NotNull CriteriaQuery<?> query, @NotNull CriteriaBuilder criteriaBuilder
    ) {
        switch (operation) {
            case GREATER_THAN:
                return criteriaBuilder.greaterThan(key, value);
            case GREATER_THAN_EQUAL:
                return criteriaBuilder.greaterThanOrEqualTo(key, value);
            case LESS_THAN_EQUAL:
                return criteriaBuilder.lessThanOrEqualTo(key, value);
            case EQUAL:
                return criteriaBuilder.equal(key, value);
            case LIKE:
                Assert.isInstanceOf(String.class, value, "Value must be a string for it to be used with LIKE");
                @SuppressWarnings("unchecked") Path<String> castKey = (Path<String>) key;
                String castValue = (String) value;
                return criteriaBuilder.like(
                        criteriaBuilder.lower(castKey),
                        "%" + castValue.toLowerCase() + "%"
                );
            default:
                throw operation.toRuntimeException();
        }
    }
}
