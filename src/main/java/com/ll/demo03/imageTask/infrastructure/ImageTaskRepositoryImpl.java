package com.ll.demo03.imageTask.infrastructure;

import com.ll.demo03.global.infrastructure.SpecificationUtils;
import com.ll.demo03.imageTask.domain.ImageTask;
import com.ll.demo03.imageTask.service.port.ImageTaskRepository;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.member.infrastructure.MemberEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ImageTaskRepositoryImpl implements ImageTaskRepository {

    private final ImageTaskJpaRepository jpaRepository;

    @Override
    public ImageTask save(ImageTask imageTask){
        return jpaRepository.save(ImageTaskEntity.from(imageTask)).toModel();
    }

    @Override
    public Optional<ImageTask> findById(Long id){
        return jpaRepository.findById(id).map(ImageTaskEntity::toModel);
    }

    @Override
    public void deleteByMemberId(Long memberId){
        jpaRepository.deleteByMemberId(memberId);
    };

    @Override
    public Slice<ImageTask> findByMember(Member creator, PageRequest pageRequest){
        return jpaRepository.findByMember(MemberEntity.from(creator), pageRequest).map(ImageTaskEntity::toModel);
    };

    @Override
    public Slice<ImageTask> findByMemberAndImageUrlIsNull(Member creator, PageRequest pageRequest){
        return jpaRepository.findByMemberAndImageUrlIsNull(MemberEntity.from(creator), pageRequest).map(ImageTaskEntity::toModel);
    };

    @Override
    public boolean existsByMemberAndCreatedAtGreaterThanAndImageUrlIsNotNull(Member creator, LocalDateTime createdAt){
        return jpaRepository.existsByMemberAndCreatedAtGreaterThanAndImageUrlIsNotNull(MemberEntity.from(creator), createdAt);
    };

    @Override
    public boolean existsByMemberAndCreatedAtGreaterThanAndImageUrlIsNull(Member creator, LocalDateTime createdAt){
        return jpaRepository.existsByMemberAndCreatedAtGreaterThanAndImageUrlIsNull(MemberEntity.from(creator), createdAt);
    };

    @Override
    public boolean existsByMemberAndCreatedAtLessThanAndImageUrlIsNotNull(Member creator, LocalDateTime createdAt){
        return jpaRepository.existsByMemberAndCreatedAtLessThanAndImageUrlIsNotNull(MemberEntity.from(creator), createdAt);
    };

    @Override
    public boolean existsByMemberAndCreatedAtLessThanAndImageUrlIsNull(Member creator, LocalDateTime createdAt){
        return jpaRepository.existsByMemberAndCreatedAtLessThanAndImageUrlIsNull(MemberEntity.from(creator), createdAt);
    };

    @Override
    public Slice<ImageTask> findCreatedAfterAndImageUrlIsNull(Member member, LocalDateTime createdAt, Pageable pageable) {
        Specification<ImageTaskEntity> spec = SpecificationUtils.memberEqualsAndCreatedAfterAndImageUrlIsNull("member", "createdAt", "imageUrl", MemberEntity.from(member), createdAt);
        return jpaRepository.findAll(spec, pageable).map(ImageTaskEntity::toModel);
    }

    @Override
    public Slice<ImageTask> findCreatedBeforeAndImageUrlIsNull(Member member, LocalDateTime createdAt, Pageable pageable) {
        Specification<ImageTaskEntity> spec = SpecificationUtils.memberEqualsAndCreatedBeforeAndImageUrlIsNull("member", "createdAt", "imageUrl", MemberEntity.from(member), createdAt);
        return jpaRepository.findAll(spec, pageable).map(ImageTaskEntity::toModel);
    }

    @Override
    public void delete(ImageTask task) {
        jpaRepository.deleteById(task.getId());
    }

    @Override
    public Slice<ImageTask> findByMemberAndImageUrlIsNotNull(Member member, PageRequest pageRequest) {
        return jpaRepository.findByMemberAndImageUrlIsNotNull(MemberEntity.from(member), pageRequest).map(ImageTaskEntity::toModel);
    }

    @Override
    public Slice<ImageTask> findCreatedAfterAndImageUrlIsNotNull(Member member, LocalDateTime createdAt, Pageable pageable) {
        Specification<ImageTaskEntity> spec = SpecificationUtils.memberEqualsAndCreatedAfterAndImageUrlIsNotNull("member", "createdAt", "imageUrl", MemberEntity.from(member), createdAt);
        return jpaRepository.findAll(spec, pageable).map(ImageTaskEntity::toModel);
    }

    @Override
    public Slice<ImageTask> findCreatedBeforeAndImageUrlIsNotNull(Member member, LocalDateTime createdAt, Pageable pageable) {
        Specification<ImageTaskEntity> spec = SpecificationUtils.memberEqualsAndCreatedBeforeAndImageUrlIsNotNull("member", "createdAt", "imageUrl", MemberEntity.from(member), createdAt);
        return jpaRepository.findAll(spec, pageable).map(ImageTaskEntity::toModel);
    }

}