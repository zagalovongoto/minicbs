package org.gimuemoa.minicbs.dto;

import jakarta.validation.constraints.*;
        import lombok.*;
        import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankTransactionDTO {

    private Long id;
    private String reference; // <MsgId> dans l'entête ISO

    // ISO 20022 : Identifiant de bout en bout unique et immuable généré à l'initiation
    private String endToEndId; // <EndToEndId> obligatoire pour le suivi transfrontalier/interbancaire

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "1.0", message = "Le montant minimum est de 1")
    private BigDecimal amount;

    @NotBlank(message = "Le type d'opération est obligatoire")
    private String type; // DEPOT, RETRAIT, VIREMENT

    @Size(max = 140, message = "Le motif ISO 20022 ne doit pas dépasser 140 caractères")
    private String description; // <RmtInf> (Remittance Information)

    // ISO 20022 Code de motif standardisé (Ex: SALA pour Salaire, PENS pour Pension)
    private String purposeCode; // <Purp> -> <Cd>

    private LocalDateTime executedAt;

    // Identification des comptes au format IBAN (ISO 13616)
    private String sourceDbtrAcct;      // Compte Débiteur (source)
    private String destinationCdtrAcct; // Compte Créditeur (destination)

    // Optionnel pour simulation interbancaire : Codes BIC des banques partenaires
    private String debtorAgentBic;      // BIC de la banque émettrice
    private String creditorAgentBic;    // BIC de la banque réceptrice
}

