package com.ll.demo03.UGC.infrastructure;

import com.ll.demo03.UGC.domain.UGC;
import com.ll.demo03.UGC.service.port.UGCRepository;
import com.ll.demo03.imageTask.domain.ImageTask;
import com.ll.demo03.imageTask.infrastructure.ImageTaskEntity;
import com.ll.demo03.videoTask.infrastructure.VideoTaskEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import java.util.List;
import java.util.Optional;


@RequiredArgsConstructor
public class UGCRepositoryImpl implements UGCRepository {

    private final UGCJpaRepository ugcJpaRepository;

    @Override
    public void delete(UGC ugc){
        ugcJpaRepository.delete(UGCEntity.from(ugc));
    }

    @Override
    public Optional<UGC> findById(Long id){
        return ugcJpaRepository.findById(id).map(UGCEntity::toModel);
    }

    @Override
    public boolean existsByIdLessThanAndMemberId(Long id, Long memberId){
        return ugcJpaRepository.existsByIdLessThanAndMemberId(id, memberId);
    }

    @Override
    public boolean existsByIdGreaterThanAndMemberId(Long id, Long memberId){
        return ugcJpaRepository.existsByIdGreaterThanAndMemberId(id, memberId);
    }

    @Override
    public Slice<UGC> findByMemberIdOrderByIdDesc(Long memberId, Pageable pageRequest){
        return ugcJpaRepository.findByMemberIdOrderByIdDesc(memberId, pageRequest).map(UGCEntity::toModel);
    }

    @Override
    public Slice<UGC> findByMemberIdAndIdLessThanOrderByIdDesc(Long memberId, Long cursorId, Pageable pageRequest){
        return ugcJpaRepository.findByMemberIdAndIdLessThanOrderByIdDesc(memberId, cursorId, pageRequest).map(UGCEntity::toModel);
    }

    @Override
    public Slice<UGC> findByMemberIdAndIdGreaterThanOrderByIdAsc(Long memberId, Long cursorId, Pageable pageRequest){
        return ugcJpaRepository.findByMemberIdAndIdGreaterThanOrderByIdAsc(memberId, cursorId, pageRequest).map(UGCEntity::toModel);
    }

    @Override
    public void deleteByMemberId(Long memberId){
        ugcJpaRepository.deleteByMemberId(memberId);
    }

    @Override
    public List<UGC> findByImageTaskIn(List<ImageTask> tasks) {
        List<ImageTaskEntity> entities = tasks.stream()
                .map(ImageTaskEntity::from)
                .toList();

        return ugcJpaRepository.findByImageTaskIn(entities)
                .stream()
                .map(UGCEntity::toModel)
                .toList();
    }


    @Override
    public List<UGC> findByVideoTaskIn(List<com.ll.demo03.videoTask.domain.VideoTask> tasks) {
        List<VideoTaskEntity> entities = tasks.stream()
                .map(VideoTaskEntity::from)
                .toList();

        return ugcJpaRepository.findByVideoTaskIn(entities)
                .stream()
                .map(UGCEntity::toModel)
                .toList();
    }

}

