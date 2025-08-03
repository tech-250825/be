package com.ll.demo03.board.infrastructure;

import com.ll.demo03.board.domain.Board;
import com.ll.demo03.board.service.port.BoardRepository;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.member.infrastructure.MemberEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class BoardRepositoryImpl implements BoardRepository {

    private final BoardJpaRepository boardJpaRepository;

    @Override
    public Board save(Board board) {
        BoardEntity entity = BoardEntity.from(board);
        BoardEntity saved = boardJpaRepository.save(entity);
        return saved.toModel();
    }

    @Override
    public Optional<Board> findById(Long id) {
        return boardJpaRepository.findById(id)
                .map(BoardEntity::toModel);
    }

    @Override
    public List<Board> findByMember(Member member) {
        return boardJpaRepository.findByMemberOrderByCreatedAtDesc(MemberEntity.from(member))
                .stream()
                .map(BoardEntity::toModel)
                .collect(Collectors.toList());
    }
}