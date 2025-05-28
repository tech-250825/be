package com.ll.demo03.domain.upscaledTask.repository;

import com.ll.demo03.domain.upscaledTask.entity.UpscaleTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UpscaleTaskRepository extends JpaRepository<UpscaleTask, Long> {
    Optional<UpscaleTask> findByNewTaskId(String taskId);

    Optional<UpscaleTask> findByTaskId(String taskId);
}
