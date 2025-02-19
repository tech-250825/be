package com.ll.demo03.domain.bookmark.controller;

import com.ll.demo03.domain.adminImage.dto.AdminImageResponse;
import com.ll.demo03.domain.bookmark.entity.Bookmark;
import com.ll.demo03.domain.bookmark.service.BookmarkService;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.oauth.entity.PrincipalDetails;
import com.ll.demo03.global.dto.GlobalResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/image")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PostMapping("/{adminImageId}/bookmark")
    public GlobalResponse addBookmark(
            @PathVariable(name="adminImageId") Long adminImageId,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Member member = principalDetails.user();
        bookmarkService.addBookmark(adminImageId, member);
        return GlobalResponse.success("북마크 등록에 성공했습니다.");
    }

    @DeleteMapping("/{adminImageId}/bookmark")
    public GlobalResponse deleteBookmark(
            @PathVariable(name="adminImageId") Long adminImageId,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Member member = principalDetails.user();
        bookmarkService.deleteBookmark(adminImageId, member);
        return GlobalResponse.success("북마크 삭제에 성공했습니다.");
    }

    @GetMapping("/bookmark/my")
    public GlobalResponse<?> getMyBookmarks(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        Member member = principalDetails.user();
        List<AdminImageResponse> bookmarks = bookmarkService.getMyBookmarks(member);
        return GlobalResponse.success(bookmarks);
    }
}
