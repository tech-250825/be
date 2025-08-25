package com.ll.demo03.invoice.domain;

import com.ll.demo03.member.domain.Member;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class Invoice {
    private final Long id;
    private final String trackId;
    private final Member member;
    private final BigDecimal amount;
    private final Status status;
    private final Currency currency;
    private final LocalDateTime approvedAt;

    @Builder
    public Invoice(Long id, String trackId, Member member, BigDecimal amount, Status status, Currency currency, LocalDateTime approvedAt){
        this.id = id;
        this.trackId = trackId;
        this.member = member;
        this.amount = amount;
        this.status = status;
        this.currency = currency;
        this.approvedAt = approvedAt;
    }

    public static Invoice from(String trackId, Member member, BigDecimal amount, Currency currency, Status status, LocalDateTime approvedAt){
        return Invoice.builder()
                .trackId(trackId)
                .member(member)
                .amount(amount)
                .status(status)
                .currency(currency)
                .approvedAt(approvedAt)
                .build();
    }

    public Invoice updateStatus(Status status) {
        return Invoice.builder()
                .id(this.id)
                .trackId(this.trackId)
                .member(this.member)
                .amount(this.amount)
                .status(status)
                .currency(this.currency)
                .approvedAt(status == Status.PAID ? LocalDateTime.now() : this.approvedAt)
                .build();
    }
}
