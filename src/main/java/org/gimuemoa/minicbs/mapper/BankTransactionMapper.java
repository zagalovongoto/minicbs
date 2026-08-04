package org.gimuemoa.minicbs.mapper;

import org.gimuemoa.minicbs.dto.BankTransactionDTO;
import org.gimuemoa.minicbs.model.BankTransaction;
import org.gimuemoa.minicbs.model.BankAccount;
import org.gimuemoa.minicbs.model.enums.EnumTransactionType;
import org.springframework.stereotype.Component;

@Component
public class BankTransactionMapper {

    public BankTransactionDTO toDto(BankTransaction txn) {
        if (txn == null) return null;

        // Extraction de la référence comme EndToId par défaut si non spécifié en base brute
        String endToEndId = "E2E-" + txn.getReference();

        return BankTransactionDTO.builder()
                .id(txn.getId())
                .reference(txn.getReference())
                .endToEndId(endToEndId)
                .amount(txn.getAmount())
                .type(txn.getType() != null ? txn.getType().name() : null)
                .description(txn.getDescription())
                .executedAt(txn.getExecutedAt())
                .sourceDbtrAcct(txn.getSourceAccount() != null ? txn.getSourceAccount().getAccountNumber() : null)
                .destinationCdtrAcct(txn.getDestinationAccount() != null ? txn.getDestinationAccount().getAccountNumber() : null)
                .purposeCode("OTHR") // Code ISO 20022 par défaut (Autre paiement)
                .build();
    }

    public BankTransaction toEntity(BankTransactionDTO dto, BankAccount source, BankAccount destination) {
        if (dto == null) return null;

        return BankTransaction.builder()
                .id(dto.getId())
                .reference(dto.getReference())
                .amount(dto.getAmount())
                .type(dto.getType() != null ? EnumTransactionType.valueOf(dto.getType()) : null)
                .description(dto.getDescription())
                .sourceAccount(source)
                .destinationAccount(destination)
                .build();
    }
}
