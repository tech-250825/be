package com.ll.demo03.global.util;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

// 동적 쿼리를 만들기 위한 인터페이스
@RequiredArgsConstructor
public class PageSpecification<T> implements Specification<T> {

    private final transient String mainFieldName;
    private final transient CursorBasedPageable cursorBasedPageable;

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        var predicate = applyPaginationFilter(root, criteriaBuilder);
        query.orderBy(criteriaBuilder.asc(root.get(mainFieldName)));

        return predicate;
    }

    private Predicate applyPaginationFilter(Root<T> root, CriteriaBuilder criteriaBuilder) {
        var searchValue = cursorBasedPageable.getSearchValue();
        if (searchValue == null) {
            return criteriaBuilder.conjunction();
        }

        return cursorBasedPageable.hasPrevPageCursor()
                //searchValue가 현재 값, mainFieldName이 정렬 기준
                ? criteriaBuilder.lessThan(root.get(mainFieldName), searchValue)
                : criteriaBuilder.greaterThan(root.get(mainFieldName), searchValue);
    }
}