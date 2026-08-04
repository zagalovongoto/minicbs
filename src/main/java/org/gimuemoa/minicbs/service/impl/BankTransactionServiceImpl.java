package org.gimuemoa.minicbs.service.impl;

import lombok.RequiredArgsConstructor;
import org.gimuemoa.minicbs.dto.BankTransactionDTO;
import org.gimuemoa.minicbs.exceptions.CustomExceptions.BusinessException;
import org.gimuemoa.minicbs.mapper.BankTransactionMapper;
import org.gimuemoa.minicbs.model.BankAccount;
import org.gimuemoa.minicbs.model.BankTransaction;
import org.gimuemoa.minicbs.model.enums.EnumAccountStatus;
import org.gimuemoa.minicbs.model.enums.EnumTransactionType;
import org.gimuemoa.minicbs.repository.BankAccountRepository;
import org.gimuemoa.minicbs.repository.BankTransactionRepository;
import org.gimuemoa.minicbs.service.BankTransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BankTransactionServiceImpl implements BankTransactionService {

    private final BankTransactionRepository transactionRepository;
    private final BankAccountRepository accountRepository;
    private final BankTransactionMapper transactionMapper;

    @Override
    public BankTransactionDTO executeTransaction(BankTransactionDTO dto) {
        EnumTransactionType txType = EnumTransactionType.valueOf(dto.getType());

        BankAccount sourceAcc = null;
        BankAccount destAcc = null;

        // 1. ANALYSE ET VALIDATION DES COMPTES SELON L'OPÉRATION
        if (txType == EnumTransactionType.RETRAIT || txType == EnumTransactionType.VIREMENT) {
            sourceAcc = accountRepository.findByAccountNumber(dto.getSourceDbtrAcct())
                    .orElseThrow(() -> new BusinessException("sourceDbtrAcct", "Le compte débiteur n'existe pas."));
            validateAccountStatus(sourceAcc);

            // Contrôle prudentiel : Vérification de la provision disponible (Solde insuffisant)
            if (sourceAcc.getBalance().compareTo(dto.getAmount()) < 0) {
                throw new BusinessException("amount", "Provision insuffisante sur le compte débiteur.");
            }
        }

        if (txType == EnumTransactionType.DEPOT || txType == EnumTransactionType.VIREMENT) {
            destAcc = accountRepository.findByAccountNumber(dto.getDestinationCdtrAcct())
                    .orElseThrow(() -> new BusinessException("destinationCdtrAcct", "Le compte créditeur n'existe pas."));
            validateAccountStatus(destAcc);
        }

        // 2. PASSAGE DES ÉCRITURES COMPTABLES (Mise à jour des soldes)
        if (sourceAcc != null) {
            sourceAcc.setBalance(sourceAcc.getBalance().subtract(dto.getAmount()));
            accountRepository.save(sourceAcc);
        }

        if (destAcc != null) {
            destAcc.setBalance(destAcc.getBalance().add(dto.getAmount()));
            accountRepository.save(destAcc);
        }

        // 3. ENREGISTREMENT COMPTABLE IMMUABLE DE LA TRANSACTION
        BankTransaction txn = transactionMapper.toEntity(dto, sourceAcc, destAcc);

        // Génération d'une référence financière unique (Standard ISO simulé : TXN-AAAAMMJJ-UUID)
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        txn.setReference("TXN-" + dateStr + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        BankTransaction savedTxn = transactionRepository.save(txn);
        return transactionMapper.toDto(savedTxn);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BankTransactionDTO> getAccountStatement(String accountNumber, int page, int size) {
        if (!accountRepository.existsByAccountNumber(accountNumber)) {
            throw new BusinessException("accountNumber", "Compte bancaire inexistant.");
        }
        Pageable pageable = PageRequest.of(page, size);
        return transactionRepository.findHistoryByAccountNumber(accountNumber, pageable)
                .map(transactionMapper::toDto);
    }

    private void validateAccountStatus(BankAccount account) {
        if (account.getStatus() != EnumAccountStatus.ACTIF) {
            throw new BusinessException("accountNumber", "L'opération est refusée : Le compte bancaire est bloqué ou clôturé.");
        }
    }
}
