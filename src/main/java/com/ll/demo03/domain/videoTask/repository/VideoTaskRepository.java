package com.ll.demo03.domain.videoTask.repository;

import com.ll.demo03.domain.videoTask.entity.VideoTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VideoTaskRepository extends JpaRepository<VideoTask, Long> {
    Optional<VideoTask> findByTaskId(String taskId);

    void deleteByMemberId(Long memberId);
}