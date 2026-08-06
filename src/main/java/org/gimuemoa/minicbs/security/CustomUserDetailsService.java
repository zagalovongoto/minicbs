package org.gimuemoa.minicbs.security;

import lombok.RequiredArgsConstructor;
import org.gimuemoa.minicbs.model.AppUser;
import org.gimuemoa.minicbs.model.enums.EnumStatut;
import org.gimuemoa.minicbs.repository.AppUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Recherche de l'utilisateur par son identifiant Email
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Aucun utilisateur trouve avec l'email : " + email));

        // Règle prudentielle : Bloquer la connexion si le gestionnaire est suspendu/inactif
        if (user.getStatut() != EnumStatut.ACTIF) {
            throw new UsernameNotFoundException("Ce compte utilisateur est actuellement suspendu.");
        }

        // Conversion des EnumRole (ex: ROLE_SUPER_ADMIN) en autorités Spring Security
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleName().name()))
                .collect(Collectors.toList());

        // Renvoie l'objet User officiel de Spring Security
        return new User(user.getEmail(), user.getPassword(), authorities);
    }


}
