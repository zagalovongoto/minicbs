package org.gimuemoa.minicbs.security;

import org.gimuemoa.minicbs.model.AppUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import java.util.Collection;

public class CustomUserDetails extends User {

    private final String nomComplet;

    public CustomUserDetails(AppUser user, Collection<? extends GrantedAuthority> authorities) {
        super(user.getEmail(), user.getPassword(), authorities);
        // Concatenation du Nom et Prénom pour l'affichage civil
        this.nomComplet = user.getPrenom() + " " + user.getNom().toUpperCase();
    }

    public String getNomComplet() {
        return this.nomComplet;
    }
}
