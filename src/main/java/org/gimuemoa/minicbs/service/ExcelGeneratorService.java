package org.gimuemoa.minicbs.service;

import org.gimuemoa.minicbs.dto.BankTransactionDTO;
import java.io.ByteArrayInputStream;
import java.util.List;

public interface ExcelGeneratorService {
    ByteArrayInputStream generateAccountStatementExcel(String accountNumber, List<BankTransactionDTO> transactions);
}
