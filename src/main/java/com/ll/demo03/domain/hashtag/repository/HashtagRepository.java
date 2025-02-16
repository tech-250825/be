package com.ll.demo03.domain.hashtag.repository;

import com.ll.demo03.domain.hashtag.entity.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, Long> {
    List<Hashtag> findByAdminImageId(Long adminImageId);

    void deleteByAdminImageId(Long adminImageId);
}
