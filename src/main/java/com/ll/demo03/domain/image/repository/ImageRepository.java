package com.ll.demo03.domain.image.repository;

import com.ll.demo03.domain.image.entity.Image;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ImageRepository extends JpaRepository<Image, Long>, JpaSpecificationExecutor<Image> {

    Slice<Image> findByFolderId(Long folderId, Pageable pageable);

    boolean existsByIdLessThan(Long id);

    boolean existsByIdLessThanAndMemberId(Long id, Long memberId);

    boolean existsByIdGreaterThanAndMemberId(Long id, Long memberId);

    Slice<Image> findByMemberIdOrderByIdDesc(Long id, Pageable pageRequest);

    Slice<Image> findByMemberIdAndIdLessThanOrderByIdDesc(Long id, Long cursorId, Pageable pageRequest);

    Slice<Image> findByMemberIdAndIdGreaterThanOrderByIdAsc(Long id, Long cursorId, Pageable pageRequest);

    boolean existsByFolderIdAndIdLessThan(Long folderId, Long id);

    boolean existsByFolderIdAndIdGreaterThan(Long folderId, Long id);

    Slice<Image> findByFolderIdOrderByIdDesc(Long folderId, PageRequest pageRequest);

    Slice<Image> findByFolderIdAndIdGreaterThanOrderByIdAsc(Long folderId, Long cursorId, PageRequest pageRequest);

    Slice<Image> findByFolderIdAndIdLessThanOrderByIdDesc(Long folderId, Long cursorId, PageRequest pageRequest);
}
