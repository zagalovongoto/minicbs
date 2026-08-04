package org.gimuemoa.minicbs.exceptions.CustomExceptions;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final String fieldName; // Exemple: "email", "telephone", ou null pour une erreur globale

    // Constructeur pour une erreur liée à un champ précis
    public BusinessException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }

    // Constructeur pour une erreur globale au-dessus du formulaire (sans champ précis)
    public BusinessException(String message) {
        super(message);
        this.fieldName = null;
    }
}
