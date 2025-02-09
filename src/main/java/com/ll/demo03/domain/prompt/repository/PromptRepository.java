package com.ll.demo03.domain.prompt.repository;

import com.ll.demo03.domain.prompt.entity.Prompt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromptRepository extends JpaRepository<Prompt, Long> {
}
