package org.gimuemoa.minicbs.model;

import jakarta.persistence.*;
import lombok.*;
import org.gimuemoa.minicbs.model.enums.EnumStatut;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "app_users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"roles", "passwordHistory"})
@ToString(exclude = {"roles", "passwordHistory"})
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nom;

    @Column(nullable = false, length = 50)
    private String prenom;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 20)
    private String telephone;

    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnumStatut statut; // ACTIVE, INACTIVE, SUSPENDED, etc.

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @Column(name = "derniere_connexion")
    private LocalDateTime derniereConnexion;

    // ==========================================================================
    // CRITÈRES DE SÉCURITÉ REQUIS PAR LA ROBUSTESSE CBS (ÉTAPE 1)
    // ==========================================================================

    @Column(name = "must_change_password", nullable = false)
    @Builder.Default
    private boolean mustChangePassword = true; // Forcé à true par défaut à la création

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt; // Permet de surveiller la politique des 90 jours

    // Liaison bidirectionnelle vers l'historique des mots de passe (Anti-rotation fictive)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AppUserPasswordHistory> passwordHistory = new ArrayList<>();

    // Liaison ManyToMany existante vers vos habilitations
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<AppRole> roles = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
        this.dateModification = LocalDateTime.now(); // Initialisé à la création
        if (this.passwordChangedAt == null) {
            this.passwordChangedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        // Dès qu'un admin modifie le gestionnaire en base, la date s'actualise toute seule
        this.dateModification = LocalDateTime.now();
    }

    // Méthode de calcul d'expiration à la volée (Règle d'audit : Expiration sous 90 jours)
    public boolean isPasswordExpired() {
        if (this.passwordChangedAt == null) return true;
        // Si la dernière modification remonte à plus de 90 jours, le mot de passe est expiré
        return this.passwordChangedAt.plusDays(90).isBefore(LocalDateTime.now());
    }
}
