package org.gimuemoa.minicbs.model;

import jakarta.persistence.*;
import lombok.*;
import org.gimuemoa.minicbs.model.enums.EnumMovementType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounting_entries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountingEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Lien vers la transaction d'origine (Piste d'audit)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private BankTransaction transaction;

    // Le compte impacté (qu'il soit client ou interne comme CAISSE/PRODUITS)
    @Column(name = "account_number", nullable = false, length = 30)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 10)
    private EnumMovementType movementType; // DEBIT ou CREDIT

    @Column(nullable = false, precision = 18, scale = 3)
    private BigDecimal amount;

    @Column(name = "entry_date", nullable = false, updatable = false)
    private LocalDateTime entryDate;

    @PrePersist
    protected void onCreate() {
        this.entryDate = LocalDateTime.now();
    }
}
