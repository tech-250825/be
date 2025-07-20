package com.ll.demo03.imageTask.infrastructure;

import com.ll.demo03.imageTask.domain.ImageTask;
import com.ll.demo03.imageTask.service.port.ImageTaskRepository;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.member.infrastructure.MemberEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ImageTaskRepositoryImpl implements ImageTaskRepository {

    private final ImageTaskJpaRepository imageTaskJpaRepository;

    @Override
    public ImageTask save(ImageTask imageTask){
        return imageTaskJpaRepository.save(ImageTaskEntity.from(imageTask)).toModel();
    }

    @Override
    public Optional<ImageTask> findById(Long id){
        return imageTaskJpaRepository.findById(id).map(ImageTaskEntity::toModel);
    }

    @Override
    public void deleteByMemberId(Long memberId){
        imageTaskJpaRepository.deleteByMemberId(memberId);
    };

    @Override
    public Slice<ImageTask> findByMember(Member creator, PageRequest pageRequest){
        return imageTaskJpaRepository.findByMember(MemberEntity.from(creator), pageRequest).map(ImageTaskEntity::toModel);
    };

    @Override
    public boolean existsByMemberAndCreatedAtGreaterThan(Member creator, LocalDateTime createdAt){
        return imageTaskJpaRepository.existsByMemberAndCreatedAtGreaterThan(MemberEntity.from(creator), createdAt);
    };

    @Override
    public boolean existsByMemberAndCreatedAtLessThan(Member creator, LocalDateTime createdAt){
        return imageTaskJpaRepository.existsByMemberAndCreatedAtLessThan(MemberEntity.from(creator), createdAt);
    };

    @Override
    public Slice<ImageTask> findAll(Specification<ImageTask> spec, PageRequest pageRequest) {
        return imageTaskJpaRepository.findAll(spec, pageRequest)
                .map(ImageTaskEntity::toModel);
    }

}
