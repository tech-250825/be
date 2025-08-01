package com.ll.demo03.UGC.service;

import com.ll.demo03.UGC.service.port.UGCCursorPaginationStrategy;
import com.ll.demo03.global.domain.PageCursors;
import com.ll.demo03.global.port.ResponseConverter;
import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.global.util.PageResponse;
import com.ll.demo03.member.domain.Member;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class UGCPaginationService {

    public <T, R> PageResponse<List<R>> getPagedContent(
            Member member,
            CursorBasedPageable pageable,
            UGCCursorPaginationStrategy<T> paginationStrategy,
            ResponseConverter<T, R> responseConverter) {

        Slice<T> page = getPage(member, pageable, paginationStrategy);
        
        return processPageResponse(page, member, pageable, paginationStrategy, responseConverter);
    }
    
    public <T, R> PageResponse<List<R>> getPagedContent(
            Member member,
            String type,
            CursorBasedPageable pageable,
            UGCCursorPaginationStrategy<T> paginationStrategy,
            ResponseConverter<T, R> responseConverter) {

        Slice<T> page = getPage(member, type, pageable, paginationStrategy);
        
        return processPageResponseWithType(page, member, type, pageable, paginationStrategy, responseConverter);
    }
    
    private <T, R> PageResponse<List<R>> processPageResponse(
            Slice<T> page,
            Member member,
            CursorBasedPageable pageable,
            UGCCursorPaginationStrategy<T> paginationStrategy,
            ResponseConverter<T, R> responseConverter) {

        if (!page.hasContent()) {
            return new PageResponse<>(Collections.emptyList(), null, null);
        }

        List<R> responses = responseConverter.convertToResponses(page.getContent());

        PageCursors cursors = paginationStrategy.createCursors(member, page.getContent(), pageable);

        return new PageResponse<>(responses, cursors.getPrevCursor(), cursors.getNextCursor());
    }
    
    private <T, R> PageResponse<List<R>> processPageResponseWithType(
            Slice<T> page,
            Member member,
            String type,
            CursorBasedPageable pageable,
            UGCCursorPaginationStrategy<T> paginationStrategy,
            ResponseConverter<T, R> responseConverter) {

        if (!page.hasContent()) {
            return new PageResponse<>(Collections.emptyList(), null, null);
        }

        List<R> responses = responseConverter.convertToResponses(page.getContent());

        PageCursors cursors = paginationStrategy.createCursors(member, page.getContent(), type, pageable);

        return new PageResponse<>(responses, cursors.getPrevCursor(), cursors.getNextCursor());
    }

    private <T> Slice<T> getPage(Member member, CursorBasedPageable pageable, UGCCursorPaginationStrategy<T> strategy) {
        if (!pageable.hasCursors()) {
            return strategy.getFirstPage(member, pageable);
        } else if (pageable.hasPrevPageCursor()) {
            return strategy.getPreviousPage(member, pageable);
        } else {
            return strategy.getNextPage(member, pageable);
        }
    }
    
    private <T> Slice<T> getPage(Member member, String type, CursorBasedPageable pageable, UGCCursorPaginationStrategy<T> strategy) {
        if (!pageable.hasCursors()) {
            return strategy.getFirstPage(member, type, pageable);
        } else if (pageable.hasPrevPageCursor()) {
            return strategy.getPreviousPage(member, type, pageable);
        } else {
            return strategy.getNextPage(member, type, pageable);
        }
    }
}