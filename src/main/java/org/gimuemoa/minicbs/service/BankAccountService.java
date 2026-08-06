package org.gimuemoa.minicbs.service;

import org.gimuemoa.minicbs.dto.BankAccountDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BankAccountService {
    BankAccountDTO openAccount(BankAccountDTO accountDTO);
    BankAccountDTO getAccountByNumber(String accountNumber);
    List<BankAccountDTO> getAccountsByClient(Long clientId);
    void updateAccountStatus(Long id, String status);
    Page<BankAccountDTO> getPaginatedAccounts(String keyword, int page, int size);
}

