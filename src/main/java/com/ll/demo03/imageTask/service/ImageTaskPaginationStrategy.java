package com.ll.demo03.imageTask.service;

import com.ll.demo03.global.domain.PageCursors;
import com.ll.demo03.global.port.CursorPaginationStrategy;
import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.imageTask.domain.ImageTask;
import com.ll.demo03.imageTask.service.port.ImageTaskRepository;
import com.ll.demo03.member.domain.Member;
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
public class ImageTaskPaginationStrategy implements CursorPaginationStrategy<ImageTask> {
    private final ImageTaskRepository imageTaskRepository;

    public ImageTaskPaginationStrategy(ImageTaskRepository imageTaskRepository) {
        this.imageTaskRepository = imageTaskRepository;
    }

    @Override
    public Slice<ImageTask> getFirstPage(Member member, CursorBasedPageable pageable) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        PageRequest pageRequest = PageRequest.of(0, pageable.getSize(), sort);
        return imageTaskRepository.findByMember(member, pageRequest);
    }

    @Override
    public Slice<ImageTask> getPreviousPage(Member member, CursorBasedPageable pageable) {
        String cursorValue = pageable.getDecodedCursor(pageable.getPrevPageCursor());
        LocalDateTime cursorCreatedAt = LocalDateTime.parse(cursorValue);

        Specification<ImageTask> spec = (root, query, cb) -> cb.and(
                cb.equal(root.get("member"), member),
                cb.greaterThan(root.get("createdAt"), cursorCreatedAt)
        );

        Sort sort = Sort.by(Sort.Direction.ASC, "createdAt");
        PageRequest pageRequest = PageRequest.of(0, pageable.getSize(), sort);
        Slice<ImageTask> taskPage = imageTaskRepository.findAll(spec, pageRequest);

        List<ImageTask> reversed = new ArrayList<>(taskPage.getContent());
        Collections.reverse(reversed);
        return new SliceImpl<>(reversed, pageRequest, taskPage.hasNext());
    }

    @Override
    public Slice<ImageTask> getNextPage(Member member, CursorBasedPageable pageable) {
        String cursorValue = pageable.getDecodedCursor(pageable.getNextPageCursor());
        LocalDateTime cursorCreatedAt = LocalDateTime.parse(cursorValue);

        Specification<ImageTask> spec = (root, query, cb) -> cb.and(
                cb.equal(root.get("member"), member),
                cb.lessThan(root.get("createdAt"), cursorCreatedAt)
        );

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        PageRequest pageRequest = PageRequest.of(0, pageable.getSize(), sort);
        return imageTaskRepository.findAll(spec, pageRequest);
    }

    @Override
    public PageCursors createCursors(Member member, List<ImageTask> content, CursorBasedPageable pageable) {
        ImageTask first = content.get(0);
        ImageTask last = content.get(content.size() - 1);

        boolean hasPrev = imageTaskRepository.existsByMemberAndCreatedAtGreaterThan(member, first.getCreatedAt());
        boolean hasNext = imageTaskRepository.existsByMemberAndCreatedAtLessThan(member, last.getCreatedAt());

        String prevCursor = pageable.getEncodedCursor(first.getCreatedAt().toString(), hasPrev);
        String nextCursor = pageable.getEncodedCursor(last.getCreatedAt().toString(), hasNext);

        return new PageCursors(prevCursor, nextCursor);
    }
}

