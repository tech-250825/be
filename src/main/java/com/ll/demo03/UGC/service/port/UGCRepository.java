package com.ll.demo03.UGC.service.port;

import com.ll.demo03.UGC.domain.UGC;
import com.ll.demo03.imageTask.domain.ImageTask;
import com.ll.demo03.videoTask.domain.VideoTask;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.Optional;

public interface UGCRepository {


    boolean existsByIdLessThanAndMemberId(Long id, Long memberId);

    boolean existsByIdGreaterThanAndMemberId(Long id, Long memberId);

    Slice<UGC> findByMemberIdOrderByIdDesc(Long id, Pageable pageRequest);

    Slice<UGC> findByMemberIdAndIdLessThanOrderByIdDesc(Long id, Long cursorId, Pageable pageRequest);

    Slice<UGC> findByMemberIdAndIdGreaterThanOrderByIdAsc(Long id, Long cursorId, Pageable pageRequest);

    void deleteByMemberId(Long memberId);

    List<UGC> findByImageTaskIn(List<ImageTask> tasks);

    List<UGC> findByVideoTaskIn(List<VideoTask> tasks);

    Optional<UGC> findById(Long imageId);

    void delete(UGC ugc);

    UGC save(UGC ugc);

    List<UGC> findAllByVideoTaskId(Long id);

    void deleteAll(List<UGC> ugcs);
}
