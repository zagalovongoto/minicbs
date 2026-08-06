package org.gimuemoa.minicbs.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountingEntryDTO {
    private Long id;
    private String transactionReference;
    private String transactionType;
    private String accountNumber;
    private String movementType; // DEBIT, CREDIT
    private BigDecimal amount;
    private LocalDateTime entryDate;
    private String accountTitle; // Nom du client ou libellé de structure
}
