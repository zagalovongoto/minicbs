package org.gimuemoa.minicbs.model;

import jakarta.persistence.*;
        import lombok.*;
import org.gimuemoa.minicbs.model.enums.EnumAccountStatus;
import org.gimuemoa.minicbs.model.enums.EnumAccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bank_accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", unique = true, nullable = false, length = 30)
    private String accountNumber; // Numéro complet (ex: CI0540100101234567890123)

    @Column(nullable = false, precision = 18, scale = 3)
    private BigDecimal balance; // Solde du compte (BigDecimal obligatoire)

    @Column(nullable = false, length = 3)
    private String currency; // Ex: "XOF"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnumAccountType type; // COURANT, EPARGNE

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnumAccountStatus status; // ACTIF, BLOQUE, CLOTURE

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.balance == null) {
            this.balance = BigDecimal.ZERO;
        }
    }
}

