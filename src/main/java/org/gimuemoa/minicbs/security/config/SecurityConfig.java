package org.gimuemoa.minicbs.security.config;

import lombok.RequiredArgsConstructor;
import org.gimuemoa.minicbs.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Active la protection granulaire @PreAuthorize sur les méthodes
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    /*@Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder); // Utilise le Bean BCrypt partagé
        return authProvider;
    }*/

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. GESTION DES AUTORISATIONS DES URLS
                .authorizeHttpRequests(auth -> auth
                        // Laisser libre accès aux assets et librairies (Bootstrap, Alpine, HTMX)
                        .requestMatchers("/css/**", "/js/**", "/vendor/**").permitAll()

                        // VERROUILLAGE ÉCRAN PARAMÉTRAGE : Strictement réservé au rôle SUPER_ADMIN
                        .requestMatchers("/settings/**").hasAuthority("ROLE_SUPER_ADMIN")

                        // Tout le reste de l'application (Clients, Comptes, Guichet) requiert une connexion
                        .anyRequest().authenticated()
                )

                // 2. CORRECTION FORM LOGIN : Syntaxe moderne Spring Boot 3.x
                /*.formLogin(form -> form
                        .defaultSuccessUrl("/clients", true) // Redirection vers le tableau de bord après succès
                        .permitAll()
                )*/
                .formLogin(form -> form
                        .loginPage("/login")             // <-- INDISPENSABLE : Appelle votre route
                        .loginProcessingUrl("/login")     // <-- INDISPENSABLE : Spring intercepte le POST ici
                        .usernameParameter("email")       // Lit le champ name="email" du formulaire
                        .passwordParameter("password")   // Lit le champ name="password" du formulaire
                        .defaultSuccessUrl("/clients", true)    // Redirige vers le dashboard après succès
                        .permitAll()
                )

                // 3. CORRECTION LOGOUT : Syntaxe moderne Spring Boot 3.x
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

}
