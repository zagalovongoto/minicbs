package org.gimuemoa.minicbs.security.config;

import lombok.RequiredArgsConstructor;
import org.gimuemoa.minicbs.security.CustomUserDetailsService;
import org.gimuemoa.minicbs.security.PasswordExpirationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Active la protection granulaire @PreAuthorize sur les méthodes
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordExpirationFilter passwordExpirationFilter;

    // AMÉLIORATION CLÉ : Enregistrement du provider natif d'authentification lié à BCrypt
    @Bean
    public org.springframework.security.authentication.dao.DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Rattachement du provider d'authentification
                .authenticationProvider(authenticationProvider())

                // INJECTION DU FILTRE PRUDENTIEL DE ROTATION (ÉTAPE 2)
                // S'exécute juste après la validation du login pour vérifier le cycle des 90 jours
                .addFilterAfter(passwordExpirationFilter, UsernamePasswordAuthenticationFilter.class)

                // 1. GESTION DES AUTORISATIONS DES URLS
                .authorizeHttpRequests(auth -> auth
                        // Laisser libre accès aux assets et aux parcours de connexion/activation
                        .requestMatchers("/css/**", "/js/**", "/vendor/**", "/login", "/login/activate/**").permitAll()

                        // Sécurisation de l'URL de modification de mot de passe (accessible aux connectés)
                        .requestMatchers("/profile/change-password").authenticated()

                        // VERROUILLAGE ÉCRAN PARAMÉTRAGE : Strictement réservé au rôle SUPER_ADMIN
                        .requestMatchers("/settings/**").hasAuthority("ROLE_SUPER_ADMIN")

                        // Tout le reste de l'application (Clients, Comptes, Guichet) requiert une connexion
                        .anyRequest().authenticated()
                )

                // 2. CONFIGURATION DU FORMULAIRE DE CONNEXION CUSTOM GIM
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("loginInput")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/clients", true) // Redirection vers les clients après succès
                        .permitAll()
                )

                // 3. GESTION DE LA CLÔTURE DE SESSION SECURISEE CSRF
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}
