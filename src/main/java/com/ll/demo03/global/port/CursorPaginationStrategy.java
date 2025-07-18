package com.ll.demo03.global.port;

import com.ll.demo03.global.domain.PageCursors;
import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.member.domain.Member;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface CursorPaginationStrategy<T> {
    Slice<T> getFirstPage(Member member, CursorBasedPageable pageable);
    Slice<T> getPreviousPage(Member member, CursorBasedPageable pageable);
    Slice<T> getNextPage(Member member, CursorBasedPageable pageable);
    PageCursors createCursors(Member member, List<T> content, CursorBasedPageable pageable);
}