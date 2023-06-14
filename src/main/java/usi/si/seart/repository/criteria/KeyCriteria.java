package usi.si.seart.repository.criteria;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import usi.si.seart.repository.operation.UnaryOperation;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public class KeyCriteria<E, T> implements Criteria<E> {

    Path<T> key;
    UnaryOperation operation;

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
