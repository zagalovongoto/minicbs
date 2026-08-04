package org.gimuemoa.minicbs.model;

import jakarta.persistence.*;
        import lombok.*;
        import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "clients")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_client", unique = true, nullable = false, length = 20)
    private String codeClient; // Ex: CLT-2026-0001

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(unique = true, nullable = false, length = 150)
    private String email;

    @Column(length = 20)
    private String telephone;

    @Column(name = "adresse_postale")
    private String adressePostale;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    // Le gestionnaire qui a créé ou qui gère ce client
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gestionnaire_id", nullable = false)
    private AppUser gestionnaire;

    // Un client peut avoir plusieurs comptes bancaires
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BankAccount> comptes;

    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
    }
}

