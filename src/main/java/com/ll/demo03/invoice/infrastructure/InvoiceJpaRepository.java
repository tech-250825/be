package com.ll.demo03.invoice.infrastructure;

import com.ll.demo03.invoice.domain.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceJpaRepository extends JpaRepository<InvoiceEntity, Long> {
    Optional<InvoiceEntity> findByTrackId(String trackId);
    List<InvoiceEntity> findByMemberIdAndStatus(Long memberId, Status status);
}