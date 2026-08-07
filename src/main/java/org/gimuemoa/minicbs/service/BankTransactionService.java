package org.gimuemoa.minicbs.service;

import org.gimuemoa.minicbs.dto.AccountingEntryDTO;
import org.gimuemoa.minicbs.dto.BankTransactionDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BankTransactionService {
    BankTransactionDTO executeTransaction(BankTransactionDTO transactionDTO);
    Page<BankTransactionDTO> getAccountStatement(String accountNumber, int page, int size);
    // Import requis : org.springframework.data.domain.Page;
    Page<BankTransactionDTO> getAllTransactionsJournal(String keyword, int page, int size, String sortBy, String direction);

    BankTransactionDTO getTransactionByReference(String reference);

    Page<AccountingEntryDTO> getGeneralLedger(String keyword, int page, int size);
    List<BankTransactionDTO> getTransactionListByAccountAndPeriod(String accountNumber, java.time.LocalDateTime start, java.time.LocalDateTime end);


}
