package com.ll.demo03.domain.imageTask.repository;

import com.ll.demo03.domain.imageTask.entity.ImageTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImageTaskRepository extends JpaRepository<ImageTask,Long> {
    Optional<ImageTask> findByTaskId(String taskId);

    void deleteByMemberId(Long memberId);
}
