package usi.si.seart.repository.criteria;

import usi.si.seart.model.GitRepo;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.util.List;

public interface Criteria {

    default List<Predicate> expand(Path<GitRepo> path, CriteriaBuilder criteriaBuilder) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Expand operation on Paths is not supported!");
    }
}