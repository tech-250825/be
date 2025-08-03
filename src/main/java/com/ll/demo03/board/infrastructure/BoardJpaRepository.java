package com.ll.demo03.board.infrastructure;

import com.ll.demo03.member.infrastructure.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardJpaRepository extends JpaRepository<BoardEntity, Long> {
    Optional<BoardEntity> findById(Long id);
    List<BoardEntity> findByMemberOrderByCreatedAtDesc(MemberEntity member);
}