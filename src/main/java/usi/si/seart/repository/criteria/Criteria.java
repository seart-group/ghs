package usi.si.seart.repository.criteria;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.util.List;

public interface Criteria<E> {

    /**
     * Expands the criteria into a list of predicates
     * @throws UnsupportedOperationException if the expanded operation is not supported.
     */
     List<Predicate> expand(Path<E> path, CriteriaBuilder criteriaBuilder);
}