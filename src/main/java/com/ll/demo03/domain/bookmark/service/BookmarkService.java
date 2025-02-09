package com.ll.demo03.domain.bookmark.service;

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

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final MemberRepository memberRepository;
    private final ImageRepository imageRepository;

    @Transactional
    public void addBookmark(Long imageId, String userEmail) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        Image image = imageRepository.findById(imageId)
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
    public List<Bookmark> getMyBookmarks(String userEmail) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        return bookmarkRepository.findByMember(member);
    }
}
