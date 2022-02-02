package usi.si.seart.gseapp.repository.specification;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.domain.Specification;
import usi.si.seart.gseapp.model.GitRepo;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GitRepoSpecification implements Specification<GitRepo> {

    List<SearchCriteria> criteriaList = new ArrayList<>();

    public void add(SearchCriteria criteria) {
        criteriaList.add(criteria);
    }

    @Override
    public Predicate toPredicate(
            @NotNull Root<GitRepo> root, @NotNull CriteriaQuery<?> query, @NotNull CriteriaBuilder criteriaBuilder
    ) {
        List<Predicate> predicates = new ArrayList<>();

        for (SearchCriteria criteria : criteriaList) {
            String key = criteria.getKey();
            Object value = criteria.getValue();
            SearchOperation operation = criteria.getOperation();

            switch (operation) {
                case GREATER_THAN:
                    predicates.add(criteriaBuilder.greaterThan(root.get(key), value.toString()));
                    break;
                case GREATER_THAN_EQUAL:
                    if (value instanceof Date) {
                        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(key), (Date) value));
                    } else {
                        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(key), value.toString()));
                    }
                    break;
                case LESS_THAN_EQUAL:
                    if (value instanceof Date) {
                        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get(key), (Date) value));
                    } else {
                        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get(key), value.toString()));
                    }
                    break;
                case EQUAL:
                    predicates.add(criteriaBuilder.equal(root.get(key), value));
                    break;
                case LIKE:
                    predicates.add(
                            criteriaBuilder.like(
                                    criteriaBuilder.lower(root.get(key)),
                                    "%" + value.toString().toLowerCase() + "%"
                            )
                    );
                    break;
                case IN:
                    predicates.add(criteriaBuilder.equal(root.join(key).get("label"), value.toString()));
                    break;
                case IS_NOT_NULL:
                    predicates.add(criteriaBuilder.isNotNull(root.get(key)));
                    break;
            }
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
