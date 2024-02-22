package ch.usi.si.seart.repository.criteria;

import org.jetbrains.annotations.NotNull;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;

public record CriteriaDisjunction<E>(List<Criteria<E>> criteria) implements Criteria<E> {

    @SafeVarargs
    public CriteriaDisjunction(Criteria<E>... criteria) {
        this(List.of(criteria));
    }

    @Override
    public Predicate toPredicate(
            @NotNull Root<E> root, @NotNull CriteriaQuery<?> query, @NotNull CriteriaBuilder criteriaBuilder
    ) {
        Predicate[] predicates = criteria.stream()
                .filter(criterion -> !(criterion instanceof AlwaysFalseCriteria<E>))
                .map(criterion -> criterion.toPredicate(root, query, criteriaBuilder))
                .toArray(Predicate[]::new);
        return criteriaBuilder.or(predicates);
    }
}
