package org.gimuemoa.minicbs.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountDTO {

    private Long id;
    private String accountNumber;

    @NotNull(message = "Le solde initial est obligatoire")
    @PositiveOrZero(message = "Le solde ne peut pas être négatif à l'ouverture")
    private BigDecimal balance;

    @NotBlank(message = "La devise est obligatoire")
    @Size(max = 3, min = 3, message = "La devise doit faire exactement 3 caractères (ex: XOF)")
    private String currency;

    @NotBlank(message = "Le type de compte est obligatoire")
    private String type; // COURANT, EPARGNE

    @NotBlank(message = "Le statut du compte est obligatoire")
    private String status; // ACTIF, BLOQUE

    private LocalDateTime createdAt;

    @NotNull(message = "Le client associé est obligatoire")
    private Long clientId;
    private String clientNomComplet;
}

