package com.ll.demo03.UGC.controller.port;

import com.ll.demo03.UGC.controller.request.UGCListRequest;
import com.ll.demo03.UGC.controller.response.UGCResponse;
import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.global.util.PageResponse;
import com.ll.demo03.member.domain.Member;
import java.util.List;

public interface UGCService {

    PageResponse<List<UGCResponse>> getMyImages(Member creator, CursorBasedPageable pageable);

    void deleteMyImages(UGCListRequest imageIds, Member creator);

}
