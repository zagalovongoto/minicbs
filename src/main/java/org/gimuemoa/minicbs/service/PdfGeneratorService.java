package org.gimuemoa.minicbs.service;

import org.gimuemoa.minicbs.dto.BankTransactionDTO;
import java.io.ByteArrayInputStream;

public interface PdfGeneratorService {
    /**
     * Génère un flux binaire PDF correspondant au reçu officiel d'une transaction.
     */
    ByteArrayInputStream generateTransactionReceipt(BankTransactionDTO transaction);
}
