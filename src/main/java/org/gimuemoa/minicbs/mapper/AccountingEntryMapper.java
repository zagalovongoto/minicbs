package org.gimuemoa.minicbs.mapper;

import org.gimuemoa.minicbs.dto.AccountingEntryDTO;
import org.gimuemoa.minicbs.model.AccountingEntry;
import org.springframework.stereotype.Component;

@Component
public class AccountingEntryMapper {

    public AccountingEntryDTO toDto(AccountingEntry entry) {
        if (entry == null) return null;

        return AccountingEntryDTO.builder()
                .id(entry.getId())
                .transactionReference(entry.getTransaction() != null ? entry.getTransaction().getReference() : null)
                .transactionType(entry.getTransaction() != null ? entry.getTransaction().getType().name() : null)
                .accountNumber(entry.getAccountNumber())
                .movementType(entry.getMovementType().name())
                .amount(entry.getAmount())
                .entryDate(entry.getEntryDate())
                .build();
    }
}
