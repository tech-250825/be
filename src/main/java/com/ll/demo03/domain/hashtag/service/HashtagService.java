package com.ll.demo03.domain.hashtag.service;

import com.ll.demo03.domain.adminImage.entity.AdminImage;
import lombok.RequiredArgsConstructor;
import com.ll.demo03.domain.hashtag.entity.Hashtag;
import com.ll.demo03.domain.hashtag.repository.HashtagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HashtagService {
    private final HashtagRepository hashtagRepository;

    @Transactional
    public void createHashtags(AdminImage adminImage, List<String> hashtagNames) {
        if (hashtagNames == null || hashtagNames.isEmpty()) {
            return;
        }

        List<Hashtag> hashtags = hashtagNames.stream()
                .map(name -> {
                    Hashtag hashtag = Hashtag.create(name, adminImage);
                    adminImage.addHashtag(hashtag);
                    return hashtag;
                })
                .collect(Collectors.toList());

        hashtagRepository.saveAll(hashtags);
    }

    @Transactional(readOnly = true)
    public List<String> getHashtagsByAdminImageId(Long adminImageId) {
        return hashtagRepository.findByAdminImageId(adminImageId)
                .stream()
                .map(Hashtag::getName)
                .toList();
    }

    @Transactional
    public void updateHashtags(AdminImage adminImage, List<String> newHashtagNames) {
        hashtagRepository.deleteByAdminImageId(adminImage.getId());
        createHashtags(adminImage, newHashtagNames);
    }

    @Transactional
    public void deleteHashtagsByPostId(Long adminImageId) {
        hashtagRepository.deleteByAdminImageId(adminImageId);
    }
}
