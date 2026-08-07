package org.gimuemoa.minicbs.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.gimuemoa.minicbs.model.AppUser;
import org.gimuemoa.minicbs.repository.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class PasswordExpirationFilter extends OncePerRequestFilter {

    private final AppUserRepository userRepository;

    public PasswordExpirationFilter(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();

        // On laisse passer librement les assets statiques, la mire de login, le logout et l'URL de changement de mot de passe
        if (requestURI.startsWith("/css/") || requestURI.startsWith("/js/") || requestURI.startsWith("/vendor/") ||
                requestURI.equals("/login") || requestURI.equals("/logout") || requestURI.equals("/login/activate") ||
                requestURI.equals("/profile/change-password")) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Si l'utilisateur est authentifié, on vérifie son statut en base de données
        if (authentication != null && authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof CustomUserDetails) {

            CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();

            // Re-lecture fraîche depuis la base SQL pour capter les modifications en temps réel
            AppUser user = userRepository.findByEmail(principal.getUsername()).orElse(null);

            if (user != null) {
                // SÉCURITÉ CBS : Si le mot de passe est expiré (90 jours) ou s'il doit obligatoirement le changer
                if (user.isMustChangePassword() || user.isPasswordExpired()) {
                    // Redirection forcée immédiate vers la page de modification
                    response.sendRedirect("/profile/change-password?reason=" +
                            (user.isMustChangePassword() ? "forced" : "expired"));
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
