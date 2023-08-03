package usi.si.seart.repository.criteria;

import org.jetbrains.annotations.NotNull;
import usi.si.seart.repository.operation.UnaryOperation;

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
        switch (operation) {
            case IS_NULL -> {
                return criteriaBuilder.isNull(key);
            }
            case IS_NOT_NULL -> {
                return criteriaBuilder.isNotNull(key);
            }
            default -> throw operation.toRuntimeException();
        }
    }
}
