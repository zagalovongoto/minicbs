package org.gimuemoa.minicbs.model;

import jakarta.persistence.*;
import lombok.*;
import org.gimuemoa.minicbs.model.enums.EnumTransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bank_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String reference; // Ex: TXN-20260804-98745

    @Column(nullable = false, precision = 18, scale = 3)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnumTransactionType type; // DEPOT, RETRAIT, VIREMENT

    @Column(length = 255)
    private String description;

    @Column(name = "executed_at", nullable = false, updatable = false)
    private LocalDateTime executedAt;

    // Compte débité (ex: null si DEPOT en espèces)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id")
    private BankAccount sourceAccount;

    // Compte crédité (ex: null si RETRAIT en espèces)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_account_id")
    private BankAccount destinationAccount;

    @PrePersist
    protected void onCreate() {
        this.executedAt = LocalDateTime.now();
    }
}
