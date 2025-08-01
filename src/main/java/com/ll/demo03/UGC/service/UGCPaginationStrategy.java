package com.ll.demo03.UGC.service;

import com.ll.demo03.UGC.domain.UGC;
import com.ll.demo03.UGC.service.port.UGCRepository;
import com.ll.demo03.UGC.service.port.UGCCursorPaginationStrategy;
import com.ll.demo03.global.domain.PageCursors;
import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.member.domain.Member;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class UGCPaginationStrategy implements UGCCursorPaginationStrategy<UGC> {
    private final UGCRepository ugcRepository;

    public UGCPaginationStrategy(UGCRepository ugcRepository) {
        this.ugcRepository = ugcRepository;
    }

    @Override
    public Slice<UGC> getFirstPage(Member member, CursorBasedPageable pageable) {
        Pageable pageRequest = PageRequest.of(0, pageable.getSize());
        return ugcRepository.findByMemberIdOrderByIdDesc(member.getId(), pageRequest);
    }

    @Override
    public Slice<UGC> getPreviousPage(Member member, CursorBasedPageable pageable) {
        Long cursorId = Long.parseLong(pageable.getDecodedCursor(pageable.getPrevPageCursor()));
        Pageable pageRequest = PageRequest.of(0, pageable.getSize());

        Slice<UGC> imageSlice = ugcRepository.findByMemberIdAndIdGreaterThanOrderByIdAsc(
                member.getId(), cursorId, pageRequest);

        List<UGC> content = new ArrayList<>(imageSlice.getContent());
        Collections.reverse(content);
        return new SliceImpl<>(content, pageRequest, imageSlice.hasNext());
    }

    @Override
    public Slice<UGC> getNextPage(Member member, CursorBasedPageable pageable) {
        Long cursorId = Long.parseLong(pageable.getDecodedCursor(pageable.getNextPageCursor()));
        Pageable pageRequest = PageRequest.of(0, pageable.getSize());

        return ugcRepository.findByMemberIdAndIdLessThanOrderByIdDesc(
                member.getId(), cursorId, pageRequest);
    }

    @Override
    public PageCursors createCursors(Member member, List<UGC> content, CursorBasedPageable pageable) {
        UGC first = content.get(0);
        UGC last = content.get(content.size() - 1);

        boolean hasPrev = ugcRepository.existsByIdGreaterThanAndMemberId(first.getId(), member.getId());
        boolean hasNext = ugcRepository.existsByIdLessThanAndMemberId(last.getId(), member.getId());

        String prevCursor = pageable.getEncodedCursor(String.valueOf(first.getId()), hasPrev);
        String nextCursor = pageable.getEncodedCursor(String.valueOf(last.getId()), hasNext);

        return new PageCursors(prevCursor, nextCursor);
    }

    @Override
    public Slice<UGC> getFirstPage(Member member, String type, CursorBasedPageable pageable) {
        Pageable pageRequest = PageRequest.of(0, pageable.getSize());
        return ugcRepository.findByMemberIdAndTypeOrderByIdDesc(member.getId(), type, pageRequest);
    }

    @Override
    public Slice<UGC> getPreviousPage(Member member, String type, CursorBasedPageable pageable) {
        Long cursorId = Long.parseLong(pageable.getDecodedCursor(pageable.getPrevPageCursor()));
        Pageable pageRequest = PageRequest.of(0, pageable.getSize());

        Slice<UGC> imageSlice = ugcRepository.findByMemberIdAndTypeAndIdGreaterThanOrderByIdAsc(
                member.getId(), type, cursorId, pageRequest);

        List<UGC> content = new ArrayList<>(imageSlice.getContent());
        Collections.reverse(content);
        return new SliceImpl<>(content, pageRequest, imageSlice.hasNext());
    }

    @Override
    public Slice<UGC> getNextPage(Member member, String type, CursorBasedPageable pageable) {
        Long cursorId = Long.parseLong(pageable.getDecodedCursor(pageable.getNextPageCursor()));
        Pageable pageRequest = PageRequest.of(0, pageable.getSize());

        return ugcRepository.findByMemberIdAndTypeAndIdLessThanOrderByIdDesc(
                member.getId(), type, cursorId, pageRequest);
    }

    @Override
    public PageCursors createCursors(Member member, List<UGC> content, String type, CursorBasedPageable pageable) {
        UGC first = content.get(0);
        UGC last = content.get(content.size() - 1);

        boolean hasPrev = ugcRepository.existsByIdGreaterThanAndMemberIdAndType(first.getId(), member.getId(), type);
        boolean hasNext = ugcRepository.existsByIdLessThanAndMemberIdAndType(last.getId(), member.getId(), type);

        String prevCursor = pageable.getEncodedCursor(String.valueOf(first.getId()), hasPrev);
        String nextCursor = pageable.getEncodedCursor(String.valueOf(last.getId()), hasNext);

        return new PageCursors(prevCursor, nextCursor);
    }
}
