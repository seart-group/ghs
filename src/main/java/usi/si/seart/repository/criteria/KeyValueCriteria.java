package usi.si.seart.repository.criteria;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import usi.si.seart.model.GitRepo;
import usi.si.seart.repository.operation.BinaryOperation;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.Attribute;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KeyValueCriteria<E, T> implements Criteria {
    Attribute<E, T> key;
    T value;
    BinaryOperation operation;

    @Override
    public List<Predicate> expand(Path<GitRepo> path, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();
        String attributeName = key.getName();
        switch (operation) {
            case GREATER_THAN:
                predicates.add(criteriaBuilder.greaterThan(path.get(attributeName), value.toString()));
                break;
            case GREATER_THAN_EQUAL:
                if (value instanceof Date) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(path.get(attributeName), (Date) value));
                } else {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(path.get(attributeName), value.toString()));
                }
                break;
            case LESS_THAN_EQUAL:
                if (value instanceof Date) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(path.get(attributeName), (Date) value));
                } else {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(path.get(attributeName), value.toString()));
                }
                break;
            case EQUAL:
                predicates.add(criteriaBuilder.equal(path.get(attributeName), value));
                break;
            case LIKE:
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(path.get(attributeName)),
                                "%" + value.toString().toLowerCase() + "%"
                        )
                );
                break;
            case IN:
                predicates.add(criteriaBuilder.equal(path.get(attributeName), value.toString()));
                break;
            default:
                throw new UnsupportedOperationException("Operation: ["+operation+"] not supported!");
        }

        return predicates;
    }
}