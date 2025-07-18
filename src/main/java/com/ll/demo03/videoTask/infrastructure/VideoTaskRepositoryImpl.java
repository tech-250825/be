package com.ll.demo03.videoTask.infrastructure;

import com.ll.demo03.member.domain.Member;
import com.ll.demo03.member.infrastructure.MemberEntity;
import com.ll.demo03.videoTask.domain.VideoTask;
import com.ll.demo03.videoTask.service.port.VideoTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class VideoTaskRepositoryImpl  implements VideoTaskRepository {

    private final VideoTaskJpaRepository videoTaskJpaRepository;

    @Override
    public VideoTask save(VideoTask videoTask){
        return videoTaskJpaRepository.save(VideoTaskEntity.from(videoTask)).toModel();
    }

    @Override
    public Optional<VideoTask> findById(Long id){
        return videoTaskJpaRepository.findById(id).map(VideoTaskEntity::toModel);
    }

    @Override
    public void deleteByMemberId(Long memberId){
        videoTaskJpaRepository.deleteByMemberId(memberId);
    };

    @Override
    public Slice<VideoTask> findByMember(Member member, PageRequest pageRequest){
        return videoTaskJpaRepository.findByMember(MemberEntity.from(member), pageRequest).map(VideoTaskEntity::toModel);
    };

    @Override
    public boolean existsByMemberAndCreatedAtGreaterThan(Member member, Long createdAt) {
        return videoTaskJpaRepository.existsByMemberAndCreatedAtGreaterThan(MemberEntity.from(member), createdAt);
    }

    @Override
    public boolean existsByMemberAndCreatedAtLessThan(Member member, Long createdAt) {
        return videoTaskJpaRepository.existsByMemberAndCreatedAtLessThan(MemberEntity.from(member), createdAt);
    }

    @Override
    public Slice<VideoTask> findAll(Specification<VideoTask> spec, PageRequest pageRequest) {
        return videoTaskJpaRepository.findAll(spec, pageRequest)
                .map(VideoTaskEntity::toModel);
    }

}
