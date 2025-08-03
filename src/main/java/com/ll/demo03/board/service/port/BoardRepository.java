package com.ll.demo03.board.service.port;

import com.ll.demo03.board.domain.Board;
import com.ll.demo03.member.domain.Member;

import java.util.List;
import java.util.Optional;

public interface BoardRepository {
    Board save(Board board);
    Optional<Board> findById(Long id);
    List<Board> findByMember(Member member);
}