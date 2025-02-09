package com.ll.demo03.domain.bookmark.controller;

import com.ll.demo03.domain.bookmark.entity.Bookmark;
import com.ll.demo03.domain.bookmark.service.BookmarkService;
import com.ll.demo03.global.dto.GlobalResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PostMapping("/{imageId}")
    public GlobalResponse addBookmark(
            @PathVariable Long imageId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        bookmarkService.addBookmark(imageId, userDetails.getUsername());
        return GlobalResponse.success();
    }

    @DeleteMapping("/{imageId}")
    public GlobalResponse deleteBookmark(
            @PathVariable Long imageId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        bookmarkService.deleteBookmark(imageId, userDetails.getUsername());
        return GlobalResponse.success();
    }

    @GetMapping("/my")
    public GlobalResponse<?> getMyBookmarks(@AuthenticationPrincipal UserDetails userDetails) {
        List<Bookmark> bookmarks = bookmarkService.getMyBookmarks(userDetails.getUsername());
        return GlobalResponse.success(bookmarks);
    }
}
