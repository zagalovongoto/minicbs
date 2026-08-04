package org.gimuemoa.minicbs.mapper;

import org.gimuemoa.minicbs.dto.BankAccountDTO;
import org.gimuemoa.minicbs.model.BankAccount;
import org.gimuemoa.minicbs.model.Client;
import org.gimuemoa.minicbs.model.enums.EnumAccountStatus;
import org.gimuemoa.minicbs.model.enums.EnumAccountType;
import org.springframework.stereotype.Component;

@Component
public class BankAccountMapper {

    public BankAccountDTO toDto(BankAccount account) {
        if (account == null) return null;

        return BankAccountDTO.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .type(account.getType() != null ? account.getType().name() : null)
                .status(account.getStatus() != null ? account.getStatus().name() : null)
                .createdAt(account.getCreatedAt())
                .clientId(account.getClient() != null ? account.getClient().getId() : null)
                .clientNomComplet(account.getClient() != null ?
                        account.getClient().getNom() + " " + account.getClient().getPrenom() : null)
                .build();
    }

    public BankAccount toEntity(BankAccountDTO dto, Client client) {
        if (dto == null) return null;

        return BankAccount.builder()
                .id(dto.getId())
                .accountNumber(dto.getAccountNumber())
                .balance(dto.getBalance())
                .currency(dto.getCurrency())
                .type(dto.getType() != null ? EnumAccountType.valueOf(dto.getType()) : null)
                .status(dto.getStatus() != null ? EnumAccountStatus.valueOf(dto.getStatus()) : null)
                .client(client)
                .build();
    }
}
