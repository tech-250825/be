package com.ll.demo03.domain.image.repository;

import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.imageTask.entity.ImageTask;
import com.ll.demo03.domain.videoTask.entity.VideoTask;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long>, JpaSpecificationExecutor<Image> {

//    @Query("SELECT i FROM Image i WHERE i.task.taskId = :taskId AND i.imgIndex = :index")
//    Optional<Image> getByTaskIdAndIndex(@Param("taskId") Long taskId, @Param("index") Integer index);

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

    Slice<Image> findByMemberIdAndVideoTaskIsNullOrderByIdDesc(Long id, Pageable pageRequest);

    Slice<Image> findByMemberIdAndVideoTaskIsNotNullOrderByIdDesc(Long id, Pageable pageRequest);

    Slice<Image> findByMemberIdAndVideoTaskIsNotNullAndIdGreaterThanOrderByIdAsc(Long id, Long cursorId, Pageable pageRequest);

    Slice<Image> findByMemberIdAndVideoTaskIsNullAndIdGreaterThanOrderByIdAsc(Long id, Long cursorId, Pageable pageRequest);

    Slice<Image> findByMemberIdAndVideoTaskIsNotNullAndIdLessThanOrderByIdDesc(Long id, Long cursorId, Pageable pageRequest);

    Slice<Image> findByMemberIdAndVideoTaskIsNullAndIdLessThanOrderByIdDesc(Long id, Long cursorId, Pageable pageRequest);

    boolean existsByIdGreaterThanAndMemberIdAndVideoTaskIsNotNull(Long id, Long id1);

    boolean existsByIdLessThanAndMemberIdAndVideoTaskIsNotNull(Long id, Long id1);

    boolean existsByIdGreaterThanAndMemberIdAndVideoTaskIsNull(Long id, Long id1);

    boolean existsByIdLessThanAndMemberIdAndVideoTaskIsNull(Long id, Long id1);

    void deleteByMemberId(Long memberId);

    List<Image> findByImageTask(ImageTask task);

    List<Image> findByVideoTask(VideoTask task);
}
