package org.gimuemoa.minicbs.controller;

import lombok.RequiredArgsConstructor;
import org.gimuemoa.minicbs.model.*;
import org.gimuemoa.minicbs.repository.*;
import org.gimuemoa.minicbs.exceptions.CustomExceptions.BusinessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/login/activate")
@RequiredArgsConstructor
public class ActivationController {

    private final ActivationTokenRepository tokenRepository;
    private final AppUserRepository userRepository;
    private final AppUserPasswordHistoryRepository passwordHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    // 1. AFFICHAGE DU FORMULAIRE D'ACTIVATION
    @GetMapping
    public String showActivationForm(@RequestParam("token") String tokenStr, Model model) {
        ActivationToken token = tokenRepository.findByToken(tokenStr)
                .orElse(null);

        // Validation réglementaire et sécuritaire du Token
        if (token == null || token.isUsed() || token.isExpired()) {
            return "redirect:/login?error=token_invalid_or_expired";
        }

        // On transmet les informations essentielles à la vue
        model.addAttribute("token", tokenStr);
        model.addAttribute("email", token.getUser().getEmail());
        model.addAttribute("usernameSuggestion", token.getUser().getUsername());
        return "login/activate"; // Va chercher templates/login/activate.html
    }

    // 2. SOUMISSION ET VERROUILLAGE DES IDENTIFIANTS
    @PostMapping
    public String processActivation(
            @RequestParam("token") String tokenStr,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {

        ActivationToken token = tokenRepository.findByToken(tokenStr).orElse(null);
        if (token == null || token.isUsed() || token.isExpired()) {
            return "redirect:/login?error=token_invalid_or_expired";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("token", tokenStr);
            model.addAttribute("email", token.getUser().getEmail());
            model.addAttribute("errorMessage", "Les mots de passe saisis ne sont pas identiques.");
            return "login/activate";
        }

        // Règle de complexité basique (Minimum 8 caractères pour le CBS)
        if (password.length() < 8) {
            model.addAttribute("token", tokenStr);
            model.addAttribute("email", token.getUser().getEmail());
            model.addAttribute("errorMessage", "Le mot de passe doit contenir au moins 8 caractères.");
            return "login/activate";
        }

        AppUser user = token.getUser();

        // Hachage officiel BCrypt
        String hashedPwd = passwordEncoder.encode(password);

        // Mise à jour du profil utilisateur (Validation des jalons de l'étape 1)
        user.setUsername(username.trim().toLowerCase());
        user.setPassword(hashedPwd);
        user.setMustChangePassword(false);
        user.setPasswordChangedAt(LocalDateTime.now());

        // Inscription de la toute première empreinte dans l'historique anti-rotation
        passwordHistoryRepository.save(AppUserPasswordHistory.builder()
                .user(user)
                .hashedPassword(hashedPwd)
                .build());

        // Neutralisation définitive du Token
        token.setUsed(true);

        userRepository.save(user);
        tokenRepository.save(token);

        return "redirect:/login?success=account_activated";
    }
}
