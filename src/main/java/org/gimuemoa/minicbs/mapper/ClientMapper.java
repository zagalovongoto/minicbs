package org.gimuemoa.minicbs.mapper;

import org.gimuemoa.minicbs.dto.ClientDTO;
import org.gimuemoa.minicbs.model.Client;
import org.gimuemoa.minicbs.model.AppUser;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {

    public ClientDTO toDto(Client client) {
        if (client == null) return null;

        return ClientDTO.builder()
                .id(client.getId())
                .codeClient(client.getCodeClient())
                .nom(client.getNom())
                .prenom(client.getPrenom())
                .email(client.getEmail())
                .telephone(client.getTelephone())
                .adressePostale(client.getAdressePostale())
                .dateCreation(client.getDateCreation())
                .gestionnaireId(client.getGestionnaire() != null ? client.getGestionnaire().getId() : null)
                .gestionnaireNomComplet(client.getGestionnaire() != null ?
                        client.getGestionnaire().getNom() + " " + client.getGestionnaire().getPrenom() : null)
                .build();
    }

    public Client toEntity(ClientDTO dto, AppUser gestionnaire) {
        if (dto == null) return null;

        return Client.builder()
                .id(dto.getId())
                .codeClient(dto.getCodeClient())
                .nom(dto.getNom())
                .prenom(dto.getPrenom())
                .email(dto.getEmail())
                .telephone(dto.getTelephone())
                .adressePostale(dto.getAdressePostale())
                .gestionnaire(gestionnaire)
                .build();
    }
}
