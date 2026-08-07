package org.gimuemoa.minicbs.security;

import org.gimuemoa.minicbs.model.AppUser;
import org.gimuemoa.minicbs.model.enums.EnumStatut;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import java.util.Collection;

public class CustomUserDetails extends User {

    private final String nomComplet;
    private final EnumStatut statut; // CORRECTIF : Déclaration de l'attribut de statut requis

    public CustomUserDetails(AppUser user, Collection<? extends GrantedAuthority> authorities) {
        super(user.getEmail(), user.getPassword(), authorities);
        // Concatenation du Nom et Prénom pour l'affichage civil
        this.nomComplet = user.getPrenom() + " " + user.getNom().toUpperCase();
        // CORRECTIF : Initialisation de la variable depuis l'entité SQL
        this.statut = user.getStatut();
    }

    public String getNomComplet() {
        return this.nomComplet;
    }

    // ==========================================================================
    // VERROUS SÉCURITAIRES NATIFS DE SPRING SECURITY
    // ==========================================================================

    @Override
    public boolean isAccountNonExpired() {
        return true; // Le compte en lui-même n'expire pas
    }

    @Override
    public boolean isAccountNonLocked() {
        // COMPTE BLOQUÉ : Si le statut en base est SUSPENDU, Spring rejette la connexion
        return this.statut != EnumStatut.SUSPENDU;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Géré par notre filtre pour la redirection /profile
    }

    @Override
    public boolean isEnabled() {
        // COMPTE ACTIF : La connexion est formellement interdite si le statut n'est pas ACTIF ou ACTIVE
        return this.statut == EnumStatut.ACTIF;
    }
}
