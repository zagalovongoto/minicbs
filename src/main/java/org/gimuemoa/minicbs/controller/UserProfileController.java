package org.gimuemoa.minicbs.controller;

import lombok.RequiredArgsConstructor;
import org.gimuemoa.minicbs.model.*;
import org.gimuemoa.minicbs.repository.*;
import org.gimuemoa.minicbs.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final AppUserRepository userRepository;
    private final AppUserPasswordHistoryRepository passwordHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    // 1. AFFICHAGE DE L'ÉCRAN
    @GetMapping("/change-password")
    public String showChangePasswordForm(@RequestParam(value = "reason", required = false) String reason, Model model) {
        if ("forced".equals(reason) || "expired".equals(reason)) {
            model.addAttribute("warningMessage", "Réglementation GIM : Votre clé d'accès a expiré ou requiert un renouvellement immédiat avant de pouvoir utiliser la console.");
            return "profile/force_change"; // <-- REDIRECTION VERS LA PAGE AUTONOME
        }
        model.addAttribute("currentPage", "profile");
        return "profile/change_password"; // Vue classique avec menu
    }


    // 2. LOGIQUE DE TRAITEMENT ET CONTRÔLE DE L'HISTORIQUE (PARTIE BLINDÉE)
    @PostMapping("/change-password")
    public String processChangePassword(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {

        AppUser user = userRepository.findByEmail(principal.getUsername()).orElseThrow();

        // Vérification 1 : Le mot de passe actuel saisi correspond-il à la base ?
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            model.addAttribute("errorMessage", "Le mot de passe actuel saisi est incorrect.");
            return "profile/change_password";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Les nouveaux mots de passe ne correspondent pas.");
            return "profile/change_password";
        }

        if (newPassword.length() < 8) {
            model.addAttribute("errorMessage", "Le nouveau mot de passe doit faire au moins 8 caractères.");
            return "profile/change_password";
        }

        // VERIFICATION 2 : ANTI-RÉUTILISATION DES ANCIENNES EMPREINTES BANCAIRES
        boolean alreadyUsed = user.getPasswordHistory().stream()
                .anyMatch(history -> passwordEncoder.matches(newPassword, history.getHashedPassword()));

        if (alreadyUsed || passwordEncoder.matches(newPassword, user.getPassword())) {
            model.addAttribute("errorMessage", "Réglementation Sécurité : Vous ne pouvez pas réutiliser un mot de passe récemment archivé dans votre historique.");
            return "profile/change_password";
        }

        // Tout est valide ! On archive l'ancien mot de passe actuel avant de le remplacer
        passwordHistoryRepository.save(AppUserPasswordHistory.builder()
                .user(user)
                .hashedPassword(user.getPassword())
                .build());

        // Sauvegarde de la nouvelle clé hachée
        String newHashedPwd = passwordEncoder.encode(newPassword);
        user.setPassword(newHashedPwd);
        user.setMustChangePassword(false);
        user.setPasswordChangedAt(LocalDateTime.now()); // Réinitialise le compteur des 90 jours
        userRepository.save(user);

        model.addAttribute("successMessage", "Votre clé d'accès a été mise à jour avec succès dans le grand livre des habilitations.");
        return "profile/change_password";
    }
}
