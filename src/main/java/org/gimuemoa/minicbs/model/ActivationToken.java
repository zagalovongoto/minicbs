package org.gimuemoa.minicbs.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activation_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String token; // La chaîne unique de sécurisation (ex: UUID)

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "is_used", nullable = false)
    @Builder.Default
    private boolean isUsed = false; // Devient vrai dès que le gestionnaire a fixé son mot de passe

    // Règle de sécurité : Vérification de l'expiration du jeton (24h)
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }
}
