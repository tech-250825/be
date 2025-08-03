package com.ll.demo03.videoTask.infrastructure;

import com.ll.demo03.board.domain.Board;
import com.ll.demo03.board.infrastructure.BoardEntity;
import com.ll.demo03.global.infrastructure.SpecificationUtils;
import com.ll.demo03.imageTask.domain.ImageTask;
import com.ll.demo03.imageTask.infrastructure.ImageTaskEntity;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.member.infrastructure.MemberEntity;
import com.ll.demo03.videoTask.domain.VideoTask;
import com.ll.demo03.videoTask.service.port.VideoTaskRepository;
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
public class VideoTaskRepositoryImpl  implements VideoTaskRepository {

    private final VideoTaskJpaRepository jpaRepository;

    @Override
    public VideoTask save(VideoTask videoTask){
        return jpaRepository.save(VideoTaskEntity.from(videoTask)).toModel();
    }

    @Override
    public Optional<VideoTask> findById(Long id){
        return jpaRepository.findById(id).map(VideoTaskEntity::toModel);
    }

    @Override
    public void deleteByMemberId(Long memberId){
        jpaRepository.deleteByMemberId(memberId);
    };

    @Override
    public Slice<VideoTask> findByMember(Member member, PageRequest pageRequest){
        return jpaRepository.findByMember(MemberEntity.from(member), pageRequest).map(VideoTaskEntity::toModel);
    };

    @Override
    public boolean existsByMemberAndCreatedAtGreaterThan(Member member, LocalDateTime createdAt) {
        return jpaRepository.existsByMemberAndCreatedAtGreaterThan(MemberEntity.from(member), createdAt);
    }

    @Override
    public boolean existsByMemberAndCreatedAtLessThan(Member member, LocalDateTime createdAt) {
        return jpaRepository.existsByMemberAndCreatedAtLessThan(MemberEntity.from(member), createdAt);
    }

    @Override
    public Slice<VideoTask> findCreatedAfter(Member member, LocalDateTime createdAt, Pageable pageable) {
        Specification<VideoTaskEntity> spec = SpecificationUtils.memberEqualsAndCreatedAfter("member", "createdAt", MemberEntity.from(member),createdAt);
        return jpaRepository.findAll(spec, pageable).map(VideoTaskEntity::toModel);
    }

    @Override
    public Slice<VideoTask> findCreatedBefore(Member member, LocalDateTime createdAt, Pageable pageable) {
        Specification<VideoTaskEntity> spec = SpecificationUtils.memberEqualsAndCreatedBefore("member", "createdAt", MemberEntity.from(member),createdAt);
        return jpaRepository.findAll(spec, pageable).map(VideoTaskEntity::toModel);
    }

    @Override
    public void delete(VideoTask task){
        jpaRepository.delete(VideoTaskEntity.from(task));
    }

    @Override
    public Slice<VideoTask> findByBoard(Board board, PageRequest pageRequest) {
        return jpaRepository.findByBoard(BoardEntity.from(board), pageRequest).map(VideoTaskEntity::toModel);
    }

    @Override
    public Optional<VideoTask> findLatestByBoard(Board board) {
        return jpaRepository.findFirstByBoardOrderByCreatedAtDesc(BoardEntity.from(board))
                .map(VideoTaskEntity::toModel);
    }
}
