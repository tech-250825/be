package com.ll.demo03.UGC.infrastructure;

import com.ll.demo03.UGC.domain.UGC;
import com.ll.demo03.UGC.service.port.UGCRepository;
import com.ll.demo03.imageTask.domain.ImageTask;
import com.ll.demo03.imageTask.infrastructure.ImageTaskEntity;
import com.ll.demo03.imageTask.infrastructure.ImageTaskJpaRepository;
import com.ll.demo03.videoTask.infrastructure.VideoTaskEntity;
import com.ll.demo03.videoTask.infrastructure.VideoTaskJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
@RequiredArgsConstructor
public class UGCRepositoryImpl implements UGCRepository {

    private final UGCJpaRepository ugcJpaRepository;
    private final ImageTaskJpaRepository imageTaskJpaRepository;
    private final VideoTaskJpaRepository videoTaskJpaRepository;

    @Override
    public UGC save(UGC ugc){
        return ugcJpaRepository.save(UGCEntity.from(ugc)).toModel();
    }

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
                .map(t -> imageTaskJpaRepository.getReferenceById(t.getId()))
                .toList();

        return ugcJpaRepository.findByImageTaskIn(entities)
                .stream()
                .map(UGCEntity::toModel)
                .toList();
    }


    @Override
    public List<UGC> findByVideoTaskIn(List<com.ll.demo03.videoTask.domain.VideoTask> tasks) {
        List<VideoTaskEntity> entities = tasks.stream()
         //       .map(t -> videoTaskJpaRepository.getReferenceById(t.getId()))
               .map(VideoTaskEntity::from) //이렇게 해도 됨.. 왠지는 알아볼 것
                .toList();

        return ugcJpaRepository.findByVideoTaskIn(entities)
                .stream()
                .map(UGCEntity::toModel)
                .toList();
    }

    @Override
    public List<UGC> findAllByVideoTaskId(Long id){
        List<UGCEntity> entities = ugcJpaRepository.findAllByVideoTask_Id(id);
        return entities.stream()
                .map(UGCEntity::toModel)
                .toList();

    }

    @Override
    public List<UGC> findAllByImageTaskId(Long id){
        List<UGCEntity> entities = ugcJpaRepository.findAllByImageTask_Id(id);
        return entities.stream()
                .map(UGCEntity::toModel)
                .toList();
    }

    @Override
    public void deleteAll(List<UGC> ugcs) {
        List<UGCEntity> entities = ugcs.stream()
                .map(UGCEntity::from)
                .toList();
        ugcJpaRepository.deleteAll(entities);
    }

    @Override
    public Slice<UGC> findByMemberIdAndTypeOrderByIdDesc(Long memberId, String type, Pageable pageRequest) {
        if (type == null) {
            return findByMemberIdOrderByIdDesc(memberId, pageRequest);
        }
        
        if ("image".equalsIgnoreCase(type)) {
            return ugcJpaRepository.findByMemberIdAndImageTaskIsNotNullAndVideoTaskIsNullOrderByIdDesc(memberId, pageRequest)
                    .map(UGCEntity::toModel);
        } else if ("video".equalsIgnoreCase(type)) {
            return ugcJpaRepository.findByMemberIdAndVideoTaskIsNotNullOrderByIdDesc(memberId, pageRequest)
                    .map(UGCEntity::toModel);
        }
        
        return findByMemberIdOrderByIdDesc(memberId, pageRequest);
    }

    @Override
    public Slice<UGC> findByMemberIdAndTypeAndIdLessThanOrderByIdDesc(Long memberId, String type, Long cursorId, Pageable pageRequest) {
        if (type == null) {
            return findByMemberIdAndIdLessThanOrderByIdDesc(memberId, cursorId, pageRequest);
        }
        
        if ("image".equalsIgnoreCase(type)) {
            return ugcJpaRepository.findByMemberIdAndImageTaskIsNotNullAndVideoTaskIsNullAndIdLessThanOrderByIdDesc(memberId, cursorId, pageRequest)
                    .map(UGCEntity::toModel);
        } else if ("video".equalsIgnoreCase(type)) {
            return ugcJpaRepository.findByMemberIdAndVideoTaskIsNotNullAndIdLessThanOrderByIdDesc(memberId, cursorId, pageRequest)
                    .map(UGCEntity::toModel);
        }
        
        return findByMemberIdAndIdLessThanOrderByIdDesc(memberId, cursorId, pageRequest);
    }

    @Override
    public Slice<UGC> findByMemberIdAndTypeAndIdGreaterThanOrderByIdAsc(Long memberId, String type, Long cursorId, Pageable pageRequest) {
        if (type == null) {
            return findByMemberIdAndIdGreaterThanOrderByIdAsc(memberId, cursorId, pageRequest);
        }
        
        if ("image".equalsIgnoreCase(type)) {
            return ugcJpaRepository.findByMemberIdAndImageTaskIsNotNullAndVideoTaskIsNullAndIdGreaterThanOrderByIdAsc(memberId, cursorId, pageRequest)
                    .map(UGCEntity::toModel);
        } else if ("video".equalsIgnoreCase(type)) {
            return ugcJpaRepository.findByMemberIdAndVideoTaskIsNotNullAndIdGreaterThanOrderByIdAsc(memberId, cursorId, pageRequest)
                    .map(UGCEntity::toModel);
        }
        
        return findByMemberIdAndIdGreaterThanOrderByIdAsc(memberId, cursorId, pageRequest);
    }

    @Override
    public boolean existsByIdGreaterThanAndMemberIdAndType(Long id, Long memberId, String type) {
        if (type == null) {
            return existsByIdGreaterThanAndMemberId(id, memberId);
        }
        
        if ("image".equalsIgnoreCase(type)) {
            return ugcJpaRepository.existsByIdGreaterThanAndMemberIdAndImageTaskIsNotNullAndVideoTaskIsNull(id, memberId);
        } else if ("video".equalsIgnoreCase(type)) {
            return ugcJpaRepository.existsByIdGreaterThanAndMemberIdAndVideoTaskIsNotNull(id, memberId);
        }
        
        return existsByIdGreaterThanAndMemberId(id, memberId);
    }

    @Override
    public boolean existsByIdLessThanAndMemberIdAndType(Long id, Long memberId, String type) {
        if (type == null) {
            return existsByIdLessThanAndMemberId(id, memberId);
        }
        
        if ("image".equalsIgnoreCase(type)) {
            return ugcJpaRepository.existsByIdLessThanAndMemberIdAndImageTaskIsNotNullAndVideoTaskIsNull(id, memberId);
        } else if ("video".equalsIgnoreCase(type)) {
            return ugcJpaRepository.existsByIdLessThanAndMemberIdAndVideoTaskIsNotNull(id, memberId);
        }
        
        return existsByIdLessThanAndMemberId(id, memberId);
    }

}

