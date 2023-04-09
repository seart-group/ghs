package usi.si.seart.repository.criteria;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import usi.si.seart.model.GitRepo;
import usi.si.seart.repository.operation.UnaryOperation;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.Attribute;
import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KeyCriteria<E, T> implements Criteria {
    Attribute<E, T> key;
    UnaryOperation operation;


    @Override
    public List<Predicate> expand(Path<GitRepo> path, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();
        switch (operation) {
            case IS_NOT_NULL:
                predicates.add(criteriaBuilder.isNotNull(path.<T>get(key.getName())));
                break;
            default:
                throw new UnsupportedOperationException("Operation: ["+operation+"] not supported!");
        }
        return predicates;
    }
}