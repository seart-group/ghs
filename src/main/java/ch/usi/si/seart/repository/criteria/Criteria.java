package ch.usi.si.seart.repository.criteria;

import ch.usi.si.seart.repository.operation.BinaryOperation;
import ch.usi.si.seart.repository.operation.TernaryOperation;
import com.google.common.collect.Range;
import org.jetbrains.annotations.NotNull;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public interface Criteria<E> {

    Predicate toPredicate(
            @NotNull Root<E> root, @NotNull CriteriaQuery<?> query, @NotNull CriteriaBuilder criteriaBuilder
    );

    static <E, T extends Comparable<T>> Criteria<E> forRange(Path<T> key, Range<T> range) {
        boolean lowerBound = range.hasLowerBound();
        boolean upperBound = range.hasUpperBound();
        if (lowerBound && upperBound) {
            T lower = range.lowerEndpoint();
            T upper = range.upperEndpoint();
            return new KeyDualValueCriteria<>(key, lower, upper, TernaryOperation.BETWEEN);
        } else if (lowerBound) {
            T lower = range.lowerEndpoint();
            return new KeyValueCriteria<>(key, lower, BinaryOperation.GREATER_THAN_EQUAL);
        } else if (upperBound) {
            T upper = range.upperEndpoint();
            return new KeyValueCriteria<>(key, upper, BinaryOperation.LESS_THAN_EQUAL);
        } else {
            return new AlwaysTrueCriteria<>();
        }
    }
}
