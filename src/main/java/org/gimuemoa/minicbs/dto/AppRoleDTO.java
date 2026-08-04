package org.gimuemoa.minicbs.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.gimuemoa.minicbs.model.AppRole;
import org.gimuemoa.minicbs.model.enums.EnumRole;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppRoleDTO {

    private Long id;

    @NotBlank(message = "Le nom du rôle est obligatoire")
    private String roleName; // Reçoit la valeur de l'Enum sous forme de String (ex: "ROLE_ADMIN")

}
