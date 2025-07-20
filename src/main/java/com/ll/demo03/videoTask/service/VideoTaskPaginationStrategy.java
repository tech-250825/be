package com.ll.demo03.videoTask.service;

import com.ll.demo03.global.domain.PageCursors;
import com.ll.demo03.global.port.CursorPaginationStrategy;
import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.imageTask.domain.ImageTask;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.videoTask.domain.VideoTask;
import com.ll.demo03.videoTask.service.port.VideoTaskRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class VideoTaskPaginationStrategy implements CursorPaginationStrategy<VideoTask> {
    private final VideoTaskRepository taskRepository;

    public VideoTaskPaginationStrategy(VideoTaskRepository videoTaskRepository) {
        this.taskRepository = videoTaskRepository;
    }

    @Override
    public Slice<VideoTask> getFirstPage(Member member, CursorBasedPageable pageable) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        PageRequest pageRequest = PageRequest.of(0, pageable.getSize(), sort);
        return taskRepository.findByMember(member, pageRequest);
    }

    @Override
    public Slice<VideoTask> getPreviousPage(Member member, CursorBasedPageable pageable) {
        String cursorValue = pageable.getDecodedCursor(pageable.getPrevPageCursor());
        LocalDateTime cursorCreatedAt = LocalDateTime.parse(cursorValue);

        Sort sort = Sort.by(Sort.Direction.ASC, "createdAt");
        PageRequest pageRequest = PageRequest.of(0, pageable.getSize(), sort);
        Slice<VideoTask> taskPage = taskRepository.findCreatedAfter(member, cursorCreatedAt, pageRequest);

        List<VideoTask> reversed = new ArrayList<>(taskPage.getContent());
        Collections.reverse(reversed);
        return new SliceImpl<>(reversed, pageRequest, taskPage.hasNext());
    }

    @Override
    public Slice<VideoTask> getNextPage(Member member, CursorBasedPageable pageable) {
        String cursorValue = pageable.getDecodedCursor(pageable.getNextPageCursor());
        LocalDateTime cursorCreatedAt = LocalDateTime.parse(cursorValue);

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        PageRequest pageRequest = PageRequest.of(0, pageable.getSize(), sort);

        return taskRepository.findCreatedBefore(member, cursorCreatedAt, pageRequest);
    }

    @Override
    public PageCursors createCursors(Member member, List<VideoTask> content, CursorBasedPageable pageable) {
        VideoTask first = content.get(0);
        VideoTask last = content.get(content.size() - 1);

        boolean hasPrev = taskRepository.existsByMemberAndCreatedAtGreaterThan(member, first.getCreatedAt());
        boolean hasNext = taskRepository.existsByMemberAndCreatedAtLessThan(member, last.getCreatedAt());

        String prevCursor = pageable.getEncodedCursor(first.getCreatedAt().toString(), hasPrev);
        String nextCursor = pageable.getEncodedCursor(last.getCreatedAt().toString(), hasNext);

        return new PageCursors(prevCursor, nextCursor);
    }
}
