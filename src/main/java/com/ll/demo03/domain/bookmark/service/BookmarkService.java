package com.ll.demo03.domain.bookmark.service;

import com.ll.demo03.domain.adminImage.dto.AdminImageResponse;
import com.ll.demo03.domain.adminImage.entity.AdminImage;
import com.ll.demo03.domain.adminImage.repository.AdminImageRepository;
import com.ll.demo03.domain.bookmark.entity.Bookmark;
import com.ll.demo03.domain.bookmark.repository.BookmarkRepository;
import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.image.repository.ImageRepository;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final MemberRepository memberRepository;
    private final AdminImageRepository adminImageRepository;

    @Transactional
    public void addBookmark(Long imageId, String userEmail) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        AdminImage image = adminImageRepository.findById(imageId)
                .orElseThrow(() -> new EntityNotFoundException("Image not found with id: " + imageId));

        Bookmark bookmark = new Bookmark();
        bookmark.setMember(member);
        bookmark.setImage(image);

        bookmarkRepository.save(bookmark);
    }

    @Transactional
    public void deleteBookmark(Long imageId, String userEmail) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        Bookmark bookmark = bookmarkRepository.findByMemberAndImageId(member, imageId)
                .orElseThrow(() -> new RuntimeException("Bookmark not found"));

        bookmarkRepository.delete(bookmark);
    }

    @Transactional
    public List<AdminImageResponse> getMyBookmarks(String userEmail) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        List<Bookmark> bookmarks = bookmarkRepository.findByMember(member);

        List<AdminImageResponse> responses = bookmarks.stream()
                .map(bookmark -> AdminImageResponse.from(bookmark.getImage())) // 각 Bookmark의 AdminImage를 AdminImageResponse로 변환
                .collect(Collectors.toList());

        return responses;
    }
}
