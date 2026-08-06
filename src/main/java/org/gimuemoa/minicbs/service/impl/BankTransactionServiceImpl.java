package org.gimuemoa.minicbs.service.impl;

import lombok.RequiredArgsConstructor;
import org.gimuemoa.minicbs.dto.AccountingEntryDTO;
import org.gimuemoa.minicbs.dto.BankTransactionDTO;
import org.gimuemoa.minicbs.exceptions.CustomExceptions.BusinessException;
import org.gimuemoa.minicbs.mapper.AccountingEntryMapper;
import org.gimuemoa.minicbs.mapper.BankTransactionMapper;
import org.gimuemoa.minicbs.model.AccountingEntry;
import org.gimuemoa.minicbs.model.BankAccount;
import org.gimuemoa.minicbs.model.BankTransaction;
import org.gimuemoa.minicbs.model.SystemParameter;
import org.gimuemoa.minicbs.model.enums.EnumAccountStatus;
import org.gimuemoa.minicbs.model.enums.EnumMovementType;
import org.gimuemoa.minicbs.model.enums.EnumTransactionType;
import org.gimuemoa.minicbs.repository.AccountingEntryRepository;
import org.gimuemoa.minicbs.repository.BankAccountRepository;
import org.gimuemoa.minicbs.repository.BankTransactionRepository;
import org.gimuemoa.minicbs.service.BankTransactionService;
import org.gimuemoa.minicbs.service.SystemParameterService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final AccountingEntryRepository accountingEntryRepository;
    private final AccountingEntryMapper accountingEntryMapper;
    private final SystemParameterService paramService; // SÉCURITÉ : Injection du service dictionnaire

    @Override
    public BankTransactionDTO executeTransaction(BankTransactionDTO dto) {
        EnumTransactionType txType = EnumTransactionType.valueOf(dto.getType());

        // EXTRACTION COMPTABLE UNIQUE DIRECTEMENT DEPUIS LA BASE DE DONNÉES (SANS RECOURS EN DUR)
        String caisseNumber = paramService.getRequiredString("GL_ACCOUNT_CAISSE");
        String produitsNumber = paramService.getRequiredString("GL_ACCOUNT_PRODUITS");
        String liaisonNumber = paramService.getRequiredString("GL_ACCOUNT_LIAISON");
        String codeEtab = paramService.getRequiredString("BANK_CODE_ETAB");

        // LECTURE DU TAUX DE FRAIS DEPUIS LA BASE
        java.math.BigDecimal rate = new java.math.BigDecimal(paramService.getRequiredString("FEE_RATE_TRANSACTION"));



        BankAccount bankCaisse = accountRepository.findByAccountNumber(caisseNumber)
                .orElseThrow(() -> new BusinessException("type", "Compte de caisse de structure introuvable."));
        BankAccount bankProduits = accountRepository.findByAccountNumber(produitsNumber)
                .orElseThrow(() -> new BusinessException("type", "Compte de produits de structure introuvable."));
        BankAccount bankLiaison = accountRepository.findByAccountNumber(liaisonNumber)
                .orElseThrow(() -> new BusinessException("type", "Compte de liaison interbancaire introuvable."));

        BankAccount sourceAcc = null;
        BankAccount destAcc = null;

        BigDecimal fees = dto.getAmount().multiply(rate).setScale(0, java.math.RoundingMode.HALF_UP);
        BigDecimal netAmount = dto.getAmount();

        // 2. ANALYSE ET VALIDATION DES COMPTES COMPTABLES SELON L'OPÉRATION
        if (txType == EnumTransactionType.RETRAIT || txType == EnumTransactionType.VIREMENT) {
            sourceAcc = accountRepository.findByAccountNumber(dto.getSourceDbtrAcct())
                    .orElseThrow(() -> new BusinessException("sourceDbtrAcct", "Le compte débiteur spécifié n'existe pas."));
            validateAccountStatus(sourceAcc);

            BigDecimal totalRequired = dto.getAmount().add(fees);
            if (sourceAcc.getBalance().compareTo(totalRequired) < 0) {
                throw new BusinessException("amount", "Provision insuffisante pour couvrir l'opération et ses frais GIM (" + fees + " XOF).");
            }
        }

        if (txType == EnumTransactionType.DEPOT) {
            destAcc = accountRepository.findByAccountNumber(dto.getDestinationCdtrAcct())
                    .orElseThrow(() -> new BusinessException("destinationCdtrAcct", "Le compte créditeur spécifié n'existe pas."));
            validateAccountStatus(destAcc);
        }

        // 3. PASSAGE DES ÉCRITURES COMPTABLES EN PARTIE DOUBLE
        if (txType == EnumTransactionType.DEPOT) {
            netAmount = dto.getAmount().subtract(fees);
            destAcc.setBalance(destAcc.getBalance().add(netAmount));
            bankCaisse.setBalance(bankCaisse.getBalance().add(dto.getAmount()));
            bankProduits.setBalance(bankProduits.getBalance().add(fees));
            accountRepository.save(destAcc);
        }
        else if (txType == EnumTransactionType.RETRAIT) {
            sourceAcc.setBalance(sourceAcc.getBalance().subtract(dto.getAmount().add(fees)));
            bankCaisse.setBalance(bankCaisse.getBalance().subtract(dto.getAmount()));
            bankProduits.setBalance(bankProduits.getBalance().add(fees));
            accountRepository.save(sourceAcc);
        }
        else if (txType == EnumTransactionType.VIREMENT) {
            sourceAcc.setBalance(sourceAcc.getBalance().subtract(dto.getAmount().add(fees)));
            bankProduits.setBalance(bankProduits.getBalance().add(fees));

            // SCÉNARIO INTERNE DYNAMIQUE : Basé sur le code établissement paramétré
            if (dto.getDestinationCdtrAcct().startsWith(codeEtab)) {
                destAcc = accountRepository.findByAccountNumber(dto.getDestinationCdtrAcct())
                        .orElseThrow(() -> new BusinessException("destinationCdtrAcct", "Le compte bénéficiaire interne est introuvable."));
                validateAccountStatus(destAcc);
                destAcc.setBalance(destAcc.getBalance().add(dto.getAmount()));
                accountRepository.save(destAcc);
            }
            // SCÉNARIO INTERBANCAIRE REGLEMENTAIRE (ISO 20022 pacs.008)
            else {
                bankLiaison.setBalance(bankLiaison.getBalance().add(dto.getAmount()));
                accountRepository.save(bankLiaison);
            }
            accountRepository.save(sourceAcc);
        }

        accountRepository.save(bankCaisse);
        accountRepository.save(bankProduits);

        // 4. ENREGISTREMENT HISTORIQUE IMMUABLE (Grand Livre Opérationnel)
        BankTransaction txn = transactionMapper.toEntity(dto, sourceAcc, destAcc);
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        txn.setReference("TXN-" + dateStr + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        txn.setDescription(dto.getDescription() + " (Frais perçus: " + fees + " XOF)");
        txn.setAmount(netAmount);

        BankTransaction savedTxn = transactionRepository.save(txn);

        // INJECTION DES ÉCRITURES DE PARTIE DOUBLE VIA PARAMÉTRAGE
        saveAccountingEntries(savedTxn,
                              txType,
                              sourceAcc != null ? sourceAcc.getAccountNumber() : null,
                              destAcc != null ? destAcc.getAccountNumber() : null,
                              dto.getAmount(),
                              netAmount,
                              fees,
                              caisseNumber,
                              produitsNumber,
                              liaisonNumber);

        return transactionMapper.toDto(savedTxn);
    }
    private void saveAccountingEntries(BankTransaction savedTxn,
                                       EnumTransactionType txType,
                                       String sourceAccNumber,
                                       String destAccNumber,
                                       BigDecimal grossAmount,
                                       BigDecimal netAmount,
                                       BigDecimal fees,
                                       String caisseNum,
                                       String produitsNum,
                                       String liaisonNum) {

        // Enregistrement systématique de la commission de la banque (CRÉDIT PRODUITS)
        if (fees.compareTo(BigDecimal.ZERO) > 0) {
            accountingEntryRepository.save(AccountingEntry.builder()
                    .transaction(savedTxn).accountNumber(produitsNum)
                    .movementType(EnumMovementType.CREDIT).amount(fees).build());
        }

        if (txType == EnumTransactionType.DEPOT) {
            // Débit Caisse (Espèces entrent) & Crédit Client (Dette de la banque augmente)
            accountingEntryRepository.save(AccountingEntry.builder()
                    .transaction(savedTxn).accountNumber(caisseNum)
                    .movementType(EnumMovementType.DEBIT).amount(grossAmount).build());

            accountingEntryRepository.save(AccountingEntry.builder()
                    .transaction(savedTxn).accountNumber(destAccNumber)
                    .movementType(EnumMovementType.CREDIT).amount(netAmount).build());
        }
        else if (txType == EnumTransactionType.RETRAIT) {
            // Débit Client (Sa dette diminue) & Crédit Caisse (Espèces sortent)
            accountingEntryRepository.save(AccountingEntry.builder()
                    .transaction(savedTxn).accountNumber(sourceAccNumber)
                    .movementType(EnumMovementType.DEBIT).amount(grossAmount.add(fees)).build());

            accountingEntryRepository.save(AccountingEntry.builder()
                    .transaction(savedTxn).accountNumber(caisseNum)
                    .movementType(EnumMovementType.CREDIT).amount(grossAmount).build());
        }
        else if (txType == EnumTransactionType.VIREMENT) {
            // Débit Client Émetteur du montant Brut
            accountingEntryRepository.save(AccountingEntry.builder()
                    .transaction(savedTxn).accountNumber(sourceAccNumber)
                    .movementType(EnumMovementType.DEBIT).amount(grossAmount.add(fees)).build());

            if (destAccNumber != null) {
                // Crédit Client Bénéficiaire Interne
                accountingEntryRepository.save(AccountingEntry.builder()
                        .transaction(savedTxn).accountNumber(destAccNumber)
                        .movementType(EnumMovementType.CREDIT).amount(grossAmount).build());
            } else {
                // Virement Externe : Crédit Compte de Liaison Interbancaire GIM/BCEAO
                accountingEntryRepository.save(AccountingEntry.builder()
                        .transaction(savedTxn).accountNumber(liaisonNum)
                        .movementType(EnumMovementType.CREDIT).amount(grossAmount).build());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BankTransactionDTO> getAllTransactionsJournal(String keyword, int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return transactionRepository.findAll(pageable).map(transactionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountingEntryDTO> getGeneralLedger(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<AccountingEntry> entryPage = (keyword != null && !keyword.trim().isEmpty()) ?
                accountingEntryRepository.searchInLedger(keyword.trim(), pageable) : accountingEntryRepository.findAll(pageable);
        return entryPage.map(accountingEntryMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BankTransactionDTO> getAccountStatement(String accountNumber, int page, int size) {
        if (!accountRepository.existsByAccountNumber(accountNumber)) {
            throw new BusinessException("accountNumber", "Compte bancaire inexistant.");
        }
        return transactionRepository.findHistoryByAccountNumber(accountNumber, PageRequest.of(page, size)).map(transactionMapper::toDto);
    }

    private void validateAccountStatus(BankAccount account) {
        if (account.getStatus() != EnumAccountStatus.ACTIF) {
            throw new BusinessException("accountNumber", "L'opération est refusée : Le compte bancaire n'est pas actif.");
        }
    }
}
