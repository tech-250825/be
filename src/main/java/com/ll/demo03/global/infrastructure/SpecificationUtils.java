package com.ll.demo03.global.infrastructure;

import com.ll.demo03.member.infrastructure.MemberEntity;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class SpecificationUtils {

    public static <T> Specification<T> memberEqualsAndCreatedAfter(String memberField, String createdAtField, MemberEntity member, LocalDateTime createdAt) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get(memberField), member),
                cb.greaterThan(root.get(createdAtField), createdAt)
        );
    }

    public static <T> Specification<T> memberEqualsAndCreatedBefore(String memberField, String createdAtField, MemberEntity member, LocalDateTime createdAt) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get(memberField), member),
                cb.lessThan(root.get(createdAtField), createdAt)
        );
    }

    public static <T> Specification<T> memberEqualsAndCreatedAfterAndImageUrlIsNotNull(String memberField, String createdAtField, String imageUrlField, MemberEntity member, LocalDateTime createdAt) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get(memberField), member),
                cb.greaterThan(root.get(createdAtField), createdAt),
                cb.isNotNull(root.get(imageUrlField))
        );
    }

    public static <T> Specification<T> memberEqualsAndCreatedBeforeAndImageUrlIsNotNull(String memberField, String createdAtField, String imageUrlField, MemberEntity member, LocalDateTime createdAt) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get(memberField), member),
                cb.lessThan(root.get(createdAtField), createdAt),
                cb.isNotNull(root.get(imageUrlField))
        );
    }

    public static <T> Specification<T> memberEqualsAndCreatedAfterAndImageUrlIsNull(String memberField, String createdAtField, String imageUrlField, MemberEntity member, LocalDateTime createdAt) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get(memberField), member),
                cb.greaterThan(root.get(createdAtField), createdAt),
                cb.isNull(root.get(imageUrlField))
        );
    }

    public static <T> Specification<T> memberEqualsAndCreatedBeforeAndImageUrlIsNull(String memberField, String createdAtField, String imageUrlField, MemberEntity member, LocalDateTime createdAt) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get(memberField), member),
                cb.lessThan(root.get(createdAtField), createdAt),
                cb.isNull(root.get(imageUrlField))
        );
    }
}