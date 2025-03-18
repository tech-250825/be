package com.ll.demo03.domain.task.repository;

import com.ll.demo03.domain.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task,Long> {
    Optional<Task> findByTaskId(String taskId);
}
