package org.gimuemoa.minicbs.mapper;

import org.gimuemoa.minicbs.dto.AppUserDTO;
import org.gimuemoa.minicbs.model.AppUser;
import org.gimuemoa.minicbs.model.enums.EnumStatut;
import org.springframework.stereotype.Component;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AppUserMapper {

    /**
     * Convertit une entité AppUser en AppUserDTO
     */
    public AppUserDTO toDto(AppUser user) {
        if (user == null) {
            return null;
        }

        Set<String> roleNames = new HashSet<>();
        if (user.getRoles() != null) {
            roleNames = user.getRoles().stream()
                    .filter(role -> role.getRoleName() != null) // Évite les NullPointerException si un rôle n'a pas de nom
                    .map(role -> role.getRoleName().name())     // Convertit l'EnumRole en String
                    .collect(Collectors.toSet());
        }

        return AppUserDTO.builder()
                .id(user.getId())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .email(user.getEmail())
                .telephone(user.getTelephone())
                .statut(user.getStatut() != null ? user.getStatut().name() : null)
                //.password(user.getPassword())
                .dateCreation(user.getDateCreation())
                .dateModification(user.getDateModification())
                .derniereConnexion(user.getDerniereConnexion())
                .roles(roleNames)
                .build();
    }

    /**
     * Convertit un AppUserDTO en entité AppUser
     */
    public AppUser toEntity(AppUserDTO dto) {
        if (dto == null) {
            return null;
        }

        return AppUser.builder()
                .id(dto.getId())
                .nom(dto.getNom())
                .prenom(dto.getPrenom())
                .email(dto.getEmail())
                .telephone(dto.getTelephone())
                .statut(dto.getStatut() != null ? EnumStatut.valueOf(dto.getStatut()) : null)
                //.password(dto.getPassword())
                .dateCreation(dto.getDateCreation())
                .dateModification(dto.getDateModification())
                .derniereConnexion(dto.getDerniereConnexion())
                .roles(new HashSet<>()) // Les rôles gérés par la relation @ManyToMany complexe sont initialisés à vide et liés au niveau du service
                .build();
    }
}
