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
    public UserDetails loadUserByUsername(String loginInput) throws UsernameNotFoundException {
        // Le paramètre loginInput reçoit la valeur saisie (que ce soit l'email ou le username)
        AppUser user = userRepository.findByUsernameOrEmail(loginInput, loginInput)
                .orElseThrow(() -> new UsernameNotFoundException("Identifiants incorrects pour : " + loginInput));

        if (user.getStatut() != EnumStatut.ACTIF) { // Ajustez selon le nom de votre Enum (ACTIF ou ACTIVE)
            throw new UsernameNotFoundException("Ce compte utilisateur est actuellement suspendu.");
        }

        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleName().name()))
                .collect(Collectors.toList());

        return new CustomUserDetails(user, authorities);
    }
}
