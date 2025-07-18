package com.ll.demo03.global.port;

import com.ll.demo03.global.domain.PageCursors;
import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.global.util.PageResponse;
import com.ll.demo03.member.domain.Member;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class CursorPaginationService {

    public <T, R> PageResponse<List<R>> getPagedContent(
            Member member,
            CursorBasedPageable pageable,
            CursorPaginationStrategy<T> paginationStrategy,
            ResponseConverter<T, R> responseConverter) {

        Slice<T> page = getPage(member, pageable, paginationStrategy);

        if (!page.hasContent()) {
            return new PageResponse<>(Collections.emptyList(), null, null);
        }

        List<R> responses = responseConverter.convertToResponses(page.getContent());

        PageCursors cursors = paginationStrategy.createCursors(member, page.getContent(), pageable);

        return new PageResponse<>(responses, cursors.getPrevCursor(), cursors.getNextCursor());
    }

    private <T> Slice<T> getPage(Member member, CursorBasedPageable pageable, CursorPaginationStrategy<T> strategy) {
        if (!pageable.hasCursors()) {
            return strategy.getFirstPage(member, pageable);
        } else if (pageable.hasPrevPageCursor()) {
            return strategy.getPreviousPage(member, pageable);
        } else {
            return strategy.getNextPage(member, pageable);
        }
    }
}