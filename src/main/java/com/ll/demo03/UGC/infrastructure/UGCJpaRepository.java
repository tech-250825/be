package com.ll.demo03.UGC.infrastructure;


import com.ll.demo03.imageTask.infrastructure.ImageTaskEntity;
import com.ll.demo03.videoTask.domain.VideoTask;
import com.ll.demo03.videoTask.infrastructure.VideoTaskEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UGCJpaRepository extends JpaRepository<UGCEntity, Long>, JpaSpecificationExecutor<UGCEntity> {

    boolean existsByIdLessThanAndMemberId(Long id, Long memberId);

    boolean existsByIdGreaterThanAndMemberId(Long id, Long memberId);

    Slice<UGCEntity> findByMemberIdOrderByIdDesc(Long id, Pageable pageRequest);

    Slice<UGCEntity> findByMemberIdAndIdLessThanOrderByIdDesc(Long id, Long cursorId, Pageable pageRequest);

    Slice<UGCEntity> findByMemberIdAndIdGreaterThanOrderByIdAsc(Long id, Long cursorId, Pageable pageRequest);

    void deleteByMemberId(Long memberId);

    @Query("SELECT u FROM UGCEntity u WHERE u.imageTask IN :tasks")
    List<UGCEntity> findByImageTaskIn(@Param("tasks") List<ImageTaskEntity> tasks);

    @Query("SELECT u FROM UGCEntity u WHERE u.videoTask IN :tasks")
    List<UGCEntity> findByVideoTaskIn(@Param("tasks") List<VideoTaskEntity> tasks);

    List<UGCEntity> findAllByVideoTask_Id(Long id);
    
    List<UGCEntity> findAllByImageTask_Id(Long id);

    // Methods for filtering by type
    Slice<UGCEntity> findByMemberIdAndImageTaskIsNotNullAndVideoTaskIsNullOrderByIdDesc(Long memberId, Pageable pageRequest);
    
    Slice<UGCEntity> findByMemberIdAndVideoTaskIsNotNullOrderByIdDesc(Long memberId, Pageable pageRequest);
    
    Slice<UGCEntity> findByMemberIdAndImageTaskIsNotNullAndVideoTaskIsNullAndIdLessThanOrderByIdDesc(Long memberId, Long cursorId, Pageable pageRequest);
    
    Slice<UGCEntity> findByMemberIdAndVideoTaskIsNotNullAndIdLessThanOrderByIdDesc(Long memberId, Long cursorId, Pageable pageRequest);
    
    Slice<UGCEntity> findByMemberIdAndImageTaskIsNotNullAndVideoTaskIsNullAndIdGreaterThanOrderByIdAsc(Long memberId, Long cursorId, Pageable pageRequest);
    
    Slice<UGCEntity> findByMemberIdAndVideoTaskIsNotNullAndIdGreaterThanOrderByIdAsc(Long memberId, Long cursorId, Pageable pageRequest);

    // Exists methods for type filtering
    boolean existsByIdGreaterThanAndMemberIdAndImageTaskIsNotNullAndVideoTaskIsNull(Long id, Long memberId);
    
    boolean existsByIdGreaterThanAndMemberIdAndVideoTaskIsNotNull(Long id, Long memberId);
    
    boolean existsByIdLessThanAndMemberIdAndImageTaskIsNotNullAndVideoTaskIsNull(Long id, Long memberId);
    
    boolean existsByIdLessThanAndMemberIdAndVideoTaskIsNotNull(Long id, Long memberId);

}
