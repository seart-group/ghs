package usi.si.seart.repository.criteria;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import usi.si.seart.model.GitRepo;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import java.util.ArrayList;
import java.util.List;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NestedKeyValueCriteria<E, T> implements Criteria<E> {
    Attribute<E, T> outerKey;
    List<Criteria> criteriaList = new ArrayList<>();

    public NestedKeyValueCriteria(Attribute<E, T> outerKey, Criteria ...criteria) {
        this.outerKey = outerKey;
        and(criteria);
    }

    NestedKeyValueCriteria<E, T> and(Criteria ...criteria) {
        this.criteriaList.addAll(List.of(criteria));
        return this;
    }

    @Override
    public List<Predicate> expand(Path<E> path, CriteriaBuilder criteriaBuilder) {
        // TODO: Add support for NestedKeyValueCriteria inside NestedKeyValueCriteria
        if(!(path instanceof Root)) {
            throw new UnsupportedOperationException("Expand operation for NestedKeyValueCriteria is only supported for Root paths!");
        }
        List<Predicate> predicates = new ArrayList<>();
        for (Criteria criteria : criteriaList) {
            predicates.addAll(criteria.expand(((Root<GitRepo>)path).join(outerKey.getName()), criteriaBuilder));
        }
        return predicates;
    }
}