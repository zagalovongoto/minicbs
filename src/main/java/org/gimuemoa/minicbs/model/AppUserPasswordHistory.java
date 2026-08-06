package org.gimuemoa.minicbs.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_password_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUserPasswordHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Lien fort vers l'utilisateur concerné
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    // Stockage de l'ancienne empreinte hachée en BCrypt
    @Column(name = "hashed_password", nullable = false, length = 255)
    private String hashedPassword;

    @Column(name = "archived_at", nullable = false)
    private LocalDateTime archivedAt;

    @PrePersist
    protected void onCreate() {
        this.archivedAt = LocalDateTime.now();
    }
}
