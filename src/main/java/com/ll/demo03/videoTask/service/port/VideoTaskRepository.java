package com.ll.demo03.videoTask.service.port;

import com.ll.demo03.board.domain.Board;
import com.ll.demo03.imageTask.domain.ImageTask;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.videoTask.domain.VideoTask;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Optional;

public interface VideoTaskRepository {

    Optional<VideoTask> findById(Long id);

    void deleteByMemberId(Long memberId);

    Slice<VideoTask> findByMember(Member member, PageRequest pageRequest);

    boolean existsByMemberAndCreatedAtGreaterThan(Member member, LocalDateTime createdAt);

    boolean existsByMemberAndCreatedAtLessThan(Member member, LocalDateTime createdAt);

    VideoTask save(VideoTask videoTask);

    Slice<VideoTask> findCreatedAfter(Member member, LocalDateTime createdAt, Pageable pageable);
    Slice<VideoTask> findCreatedBefore(Member member, LocalDateTime createdAt, Pageable pageable);

    void delete(VideoTask task);

    Slice<VideoTask> findByBoard(Board board, PageRequest pageRequest);
    
    Optional<VideoTask> findLatestByBoard(Board board);
}
