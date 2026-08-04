package org.gimuemoa.minicbs.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDTO {

    private Long id;
    private String codeClient;

    @NotBlank(message = "Le nom du client est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String nom;

    @NotBlank(message = "Le prénom du client est obligatoire")
    @Size(max = 100, message = "Le prénom ne doit pas dépasser 100 caractères")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Le format de l'email est invalide")
    private String email;

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    private String telephone;

    private String adressePostale;
    private LocalDateTime dateCreation;

    // Identifiant du gestionnaire en charge
    @NotNull(message = "Le gestionnaire associé est obligatoire")
    private Long gestionnaireId;
    private String gestionnaireNomComplet; // Pratique pour l'affichage dans la liste des clients
}
