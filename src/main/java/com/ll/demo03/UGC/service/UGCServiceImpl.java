package com.ll.demo03.UGC.service;

import com.ll.demo03.UGC.controller.port.UGCService;
import com.ll.demo03.UGC.controller.response.UGCResponse;
import com.ll.demo03.UGC.service.port.UGCRepository;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.global.util.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UGCServiceImpl  implements UGCService {

    private final UGCPaginationService paginationService;
    private final UGCPaginationStrategy paginationStrategy;
    private final UGCResponseConverter responseConverter;

    @Transactional(readOnly = true)
    public PageResponse<List<UGCResponse>> getMyImages(Member creator, String type, CursorBasedPageable pageable) {
        return paginationService.getPagedContent(creator, type, pageable, paginationStrategy, responseConverter);
    }

}