package com.ll.demo03.board.service;

import com.ll.demo03.UGC.domain.UGC;
import com.ll.demo03.UGC.service.port.UGCRepository;
import com.ll.demo03.board.controller.port.BoardService;
import com.ll.demo03.board.controller.request.BoardCreateRequest;
import com.ll.demo03.board.controller.response.BoardResponse;
import com.ll.demo03.board.domain.Board;
import com.ll.demo03.board.service.port.BoardRepository;
import com.ll.demo03.global.domain.Status;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.videoTask.controller.response.TaskOrVideoResponse;
import com.ll.demo03.videoTask.domain.VideoTask;
import com.ll.demo03.videoTask.service.port.VideoTaskRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final VideoTaskRepository videoTaskRepository;
    private final UGCRepository ugcRepository;

    @Override
    public BoardResponse create(BoardCreateRequest request, Member member) {
        Board board = Board.builder()
                .name(request.getName())
                .member(member)
                .build();
        
        Board savedBoard = boardRepository.save(board);
        return BoardResponse.from(savedBoard);
    }

    @Override
    public List<BoardResponse> getMyBoards(Member member) {
        List<Board> boards = boardRepository.findByMember(member);
        
        return boards.stream()
                .map(board -> {
                    Optional<VideoTask> latestVideoTask = videoTaskRepository.findLatestByBoard(board);
                    
                    if (latestVideoTask.isPresent()) {
                        VideoTask task = latestVideoTask.get();
                        TaskOrVideoResponse taskResponse = createTaskOrVideoResponse(task);
                        return BoardResponse.from(board, taskResponse);
                    } else {
                        return BoardResponse.from(board);
                    }
                })
                .collect(Collectors.toList());
    }

    private TaskOrVideoResponse createTaskOrVideoResponse(VideoTask task) {
        if (Status.COMPLETED.equals(task.getStatus())) {
            // 완료된 태스크의 경우 UGC를 찾아서 VideoResponse로 만들기
            List<UGC> ugcs = ugcRepository.findAllByVideoTaskId(task.getId());
            if (!ugcs.isEmpty()) {
                UGC firstUgc = ugcs.get(0);
                return TaskOrVideoResponse.fromVideo(task, firstUgc);
            }
        }
        // 진행중이거나 실패한 태스크, 또는 UGC가 없는 경우 TaskResponse로 만들기
        return TaskOrVideoResponse.fromTask(task);
    }
}