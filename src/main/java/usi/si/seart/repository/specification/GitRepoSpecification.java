package usi.si.seart.repository.specification;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.domain.Specification;
import usi.si.seart.model.GitRepo;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GitRepoSpecification implements Specification<GitRepo> {

    GitRepoSearch search;

    @Override
    public Predicate toPredicate(
            @NotNull Root<GitRepo> root, @NotNull CriteriaQuery<?> query, @NotNull CriteriaBuilder criteriaBuilder
    ) {
        Predicate[] predicates = search.toCriteriaList(root).stream()
                .map(criteria -> criteria.toPredicate(root, query, criteriaBuilder))
                .toArray(Predicate[]::new);

        query.distinct(true);
        return criteriaBuilder.and(predicates);
    }
}
