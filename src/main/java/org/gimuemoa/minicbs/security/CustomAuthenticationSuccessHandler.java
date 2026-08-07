package org.gimuemoa.minicbs.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.gimuemoa.minicbs.model.AppUser;
import org.gimuemoa.minicbs.repository.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final AppUserRepository userRepository;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) principal;

            // ÉCRITURE DIRECTE ET SOUVERAINE EN BASE DE DONNÉES
            AppUser user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
            if (user != null) {
                user.setDerniereConnexion(LocalDateTime.now());
                userRepository.saveAndFlush(user); // Force l'écriture SQL immédiate
                System.out.println(">>> [AUDIT SÉCURITÉ SUCCESS] Derniere connexion gravee pour : " + user.getEmail());
            }
        }

        // Redirection par défaut vers l'écran des clients (comme configuré dans votre projet)
        response.sendRedirect("/");
    }
}
