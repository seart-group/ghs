package usi.si.seart.repository.criteria;

import org.jetbrains.annotations.NotNull;
import usi.si.seart.repository.operation.UnaryOperation;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public record KeyCriteria<E, T>(Path<T> key, UnaryOperation operation) implements Criteria<E> {

    @Override
    public Predicate toPredicate(
            @NotNull Root<E> root, @NotNull CriteriaQuery<?> query, @NotNull CriteriaBuilder criteriaBuilder
    ) {
        return switch (operation) {
            case IS_NULL -> criteriaBuilder.isNull(key);
            case IS_NOT_NULL -> criteriaBuilder.isNotNull(key);
        };
    }
}
