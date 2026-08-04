package org.gimuemoa.minicbs.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.gimuemoa.minicbs.model.AppUser;
import org.gimuemoa.minicbs.model.enums.EnumStatut;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUserDTO {

    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100, message = "Le prénom ne doit pas dépasser 100 caractères")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Le format de l'email est invalide")
    @Size(max = 150, message = "L'email ne doit pas dépasser 150 caractères")
    private String email;

    @Size(max = 20, message = "Le numéro de téléphone ne doit pas dépasser 20 caractères")
    @NotBlank(message = "Le téléphone est obligatoire")
    private String telephone;

    private String photo;

    @NotNull(message = "Le statut est obligatoire")
    private String statut; // Reçoit la valeur de l'Enum (ex: "ACTIF", "INACTIF")

    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private LocalDateTime derniereConnexion;

    private Set<String> roles;

}
