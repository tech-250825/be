package com.ll.demo03.invoice.infrastructure;

import com.ll.demo03.invoice.domain.Invoice;
import com.ll.demo03.invoice.service.port.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class InvoiceRepositoryImpl implements InvoiceRepository {

    private final InvoiceJpaRepository invoiceJpaRepository;

    @Override
    public Invoice save(Invoice invoice){
        return invoiceJpaRepository.save(InvoiceEntity.from(invoice)).toModel();
    }

    @Override
    public Optional<Invoice> findByTrackId(String trackId) {
        return invoiceJpaRepository.findByTrackId(trackId)
                .map(InvoiceEntity::toModel);
    }
}
