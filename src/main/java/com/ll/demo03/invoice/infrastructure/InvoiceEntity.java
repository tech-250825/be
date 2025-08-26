package com.ll.demo03.invoice.infrastructure;

import com.ll.demo03.invoice.domain.Currency;
import com.ll.demo03.invoice.domain.Invoice;
import com.ll.demo03.invoice.domain.Status;
import com.ll.demo03.member.infrastructure.MemberEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class InvoiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String trackId;

    private String orderId;

    @ManyToOne
    private MemberEntity member;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private Currency currency;

    @Column(nullable = false)
    private LocalDateTime approvedAt;

    public static InvoiceEntity from(Invoice invoice){
        InvoiceEntity invoiceEntity = new InvoiceEntity();
        invoiceEntity.id = invoice.getId();
        invoiceEntity.trackId = invoice.getTrackId();
        invoiceEntity.member = MemberEntity.from(invoice.getMember());
        invoiceEntity.amount = invoice.getAmount();
        invoiceEntity.status = invoice.getStatus();
        invoiceEntity.currency = invoice.getCurrency();
        invoiceEntity.approvedAt = invoice.getApprovedAt();

        return invoiceEntity;
    }

    public Invoice toModel(){
        return Invoice.builder()
                .id(this.getId())
                .trackId(this.getTrackId())
                .member(this.getMember().toModel())
                .amount(this.getAmount())
                .status(this.getStatus())
                .currency(this.getCurrency())
                .build();
    }
}
