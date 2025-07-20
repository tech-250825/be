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
    public boolean existsByMemberAndCreatedAtGreaterThan(Member creator, LocalDateTime createdAt){
        return jpaRepository.existsByMemberAndCreatedAtGreaterThan(MemberEntity.from(creator), createdAt);
    };

    @Override
    public boolean existsByMemberAndCreatedAtLessThan(Member creator, LocalDateTime createdAt){
        return jpaRepository.existsByMemberAndCreatedAtLessThan(MemberEntity.from(creator), createdAt);
    };

    @Override
    public Slice<ImageTask> findCreatedAfter(Member member, LocalDateTime createdAt, Pageable pageable) {
        Specification<ImageTaskEntity> spec = SpecificationUtils.memberEqualsAndCreatedAfter("member", "createdAt", MemberEntity.from(member),createdAt);
        return jpaRepository.findAll(spec, pageable).map(ImageTaskEntity::toModel);
    }

    @Override
    public Slice<ImageTask> findCreatedBefore(Member member, LocalDateTime createdAt, Pageable pageable) {
        Specification<ImageTaskEntity> spec = SpecificationUtils.memberEqualsAndCreatedBefore("member", "createdAt", MemberEntity.from(member),createdAt);
        return jpaRepository.findAll(spec, pageable).map(ImageTaskEntity::toModel);
    }


}
