package com.ll.demo03.board.controller.port;

import com.ll.demo03.board.controller.request.BoardCreateRequest;
import com.ll.demo03.board.controller.response.BoardResponse;
import com.ll.demo03.member.domain.Member;

import java.util.List;

public interface BoardService {
    BoardResponse create(BoardCreateRequest request, Member member);
    List<BoardResponse> getMyBoards(Member member);
}