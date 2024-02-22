package ch.usi.si.seart.repository.criteria;

import ch.usi.si.seart.repository.operation.UnaryOperation;
import org.jetbrains.annotations.NotNull;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public record KeyCriteria<E, T>(Path<T> key, UnaryOperation operation) implements Criteria<E> {

    @Override
    public Predicate toPredicate(
            @NotNull Root<E> root, @NotNull CriteriaQuery<?> query, @NotNull CriteriaBuilder criteriaBuilder
    ) {
        return switch (operation) {
            case IS_NULL -> criteriaBuilder.isNull(key);
            case IS_NOT_NULL -> criteriaBuilder.isNotNull(key);
            case IS_TRUE -> criteriaBuilder.isTrue(key.as(Boolean.class));
            case IS_FALSE -> criteriaBuilder.isFalse(key.as(Boolean.class));
        };
    }
}
