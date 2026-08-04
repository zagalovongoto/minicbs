package org.gimuemoa.minicbs.service;

import org.gimuemoa.minicbs.dto.BankTransactionDTO;
import org.springframework.data.domain.Page;

public interface BankTransactionService {
    BankTransactionDTO executeTransaction(BankTransactionDTO transactionDTO);
    Page<BankTransactionDTO> getAccountStatement(String accountNumber, int page, int size);
}
