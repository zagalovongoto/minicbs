package org.gimuemoa.minicbs.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "system_parameter")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemParameter {

    @Id
    @Column(name = "param_key", length = 50, nullable = false, unique = true)
    private String paramKey; // Ex: BANK_BIN, FEE_RATE_TRANSACTION

    @Column(name = "param_value", length = 255, nullable = false)
    private String paramValue; // Stocké en String pour la généricité (à convertir au besoin)

    @Column(length = 255)
    private String description; // Utile pour l'auditeur ou l'administrateur technique

    @Column(name = "is_editable", nullable = false)
    private boolean isEditable = true; // Empêche la modification de certains paramètres vitaux
}
