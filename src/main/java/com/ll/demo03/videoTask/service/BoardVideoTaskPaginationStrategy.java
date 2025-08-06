package com.ll.demo03.videoTask.service;

import com.ll.demo03.board.domain.Board;
import com.ll.demo03.global.domain.PageCursors;
import com.ll.demo03.global.port.CursorPaginationStrategy;
import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.videoTask.domain.VideoTask;
import com.ll.demo03.videoTask.service.port.VideoTaskRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Component
@RequiredArgsConstructor
public class BoardVideoTaskPaginationStrategy implements CursorPaginationStrategy<VideoTask> {
    private final VideoTaskRepository taskRepository;
    private Board board;

    @Override
    public Slice<VideoTask> getFirstPage(Member member, CursorBasedPageable pageable) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        PageRequest pageRequest = PageRequest.of(0, pageable.getSize(), sort);
        Slice<VideoTask> allTasks = taskRepository.findByMember(member, pageRequest);

        List<VideoTask> filteredTasks = allTasks.getContent().stream()
                .filter(task -> task.getBoard() != null && task.getBoard().getId().equals(board.getId()))
                .collect(Collectors.toList());
        
        return new SliceImpl<>(filteredTasks, pageRequest, hasMoreAfter(member, filteredTasks));
    }

    @Override
    public Slice<VideoTask> getPreviousPage(Member member, CursorBasedPageable pageable) {
        String cursorValue = pageable.getDecodedCursor(pageable.getPrevPageCursor());
        LocalDateTime cursorCreatedAt = LocalDateTime.parse(cursorValue);

        Sort sort = Sort.by(Sort.Direction.ASC, "createdAt");
        PageRequest pageRequest = PageRequest.of(0, pageable.getSize(), sort);
        Slice<VideoTask> taskPage = taskRepository.findCreatedAfter(member, cursorCreatedAt, pageRequest);

        List<VideoTask> filteredTasks = taskPage.getContent().stream()
                .filter(task -> task.getBoard() != null && task.getBoard().getId().equals(board.getId()))
                .collect(Collectors.toList());

        Collections.reverse(filteredTasks);
        return new SliceImpl<>(filteredTasks, pageRequest, hasMoreAfter(member, filteredTasks));
    }

    @Override
    public Slice<VideoTask> getNextPage(Member member, CursorBasedPageable pageable) {
        String cursorValue = pageable.getDecodedCursor(pageable.getNextPageCursor());
        LocalDateTime cursorCreatedAt = LocalDateTime.parse(cursorValue);

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        PageRequest pageRequest = PageRequest.of(0, pageable.getSize(), sort);
        Slice<VideoTask> taskPage = taskRepository.findCreatedBefore(member, cursorCreatedAt, pageRequest);

        List<VideoTask> filteredTasks = taskPage.getContent().stream()
                .filter(task -> task.getBoard() != null && task.getBoard().getId().equals(board.getId()))
                .collect(Collectors.toList());

        return new SliceImpl<>(filteredTasks, pageRequest, hasMoreAfter(member, filteredTasks));
    }

    @Override
    public PageCursors createCursors(Member member, List<VideoTask> content, CursorBasedPageable pageable) {
        if (content.isEmpty()) {
            return new PageCursors(null, null);
        }

        VideoTask first = content.get(0);
        VideoTask last = content.get(content.size() - 1);

        boolean hasPrev = hasMoreBefore(member, first.getCreatedAt());
        boolean hasNext = hasMoreAfter(member, List.of(last));

        String prevCursor = pageable.getEncodedCursor(first.getCreatedAt().toString(), hasPrev);
        String nextCursor = pageable.getEncodedCursor(last.getCreatedAt().toString(), hasNext);

        return new PageCursors(prevCursor, nextCursor);
    }

    private boolean hasMoreBefore(Member member, LocalDateTime createdAt) {
        Slice<VideoTask> tasks = taskRepository.findCreatedAfter(member, createdAt, PageRequest.of(0, 1));
        return tasks.getContent().stream()
                .anyMatch(task -> task.getBoard() != null && task.getBoard().getId().equals(board.getId()));
    }

    private boolean hasMoreAfter(Member member, List<VideoTask> currentTasks) {
        if (currentTasks.isEmpty()) {
            return false;
        }
        LocalDateTime lastCreatedAt = currentTasks.get(currentTasks.size() - 1).getCreatedAt();
        Slice<VideoTask> tasks = taskRepository.findCreatedBefore(member, lastCreatedAt, PageRequest.of(0, 1));
        return tasks.getContent().stream()
                .anyMatch(task -> task.getBoard() != null && task.getBoard().getId().equals(board.getId()));
    }
}