package org.gimuemoa.minicbs.service.impl;

import lombok.RequiredArgsConstructor;
import org.gimuemoa.minicbs.dto.ClientDTO;
import org.gimuemoa.minicbs.exceptions.CustomExceptions.BusinessException;
import org.gimuemoa.minicbs.mapper.ClientMapper;
import org.gimuemoa.minicbs.model.AppUser;
import org.gimuemoa.minicbs.model.Client;
import org.gimuemoa.minicbs.repository.AppUserRepository;
import org.gimuemoa.minicbs.repository.ClientRepository;
import org.gimuemoa.minicbs.service.ClientService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final AppUserRepository userRepository;
    private final ClientMapper clientMapper;

    @Override
    public ClientDTO createClient(ClientDTO clientDTO) {
        if (clientRepository.existsByEmail(clientDTO.getEmail())) {
            throw new BusinessException("email", "Cette adresse email appartient déjà à un autre client.");
        }

        AppUser gestionnaire = userRepository.findById(clientDTO.getGestionnaireId())
                .orElseThrow(() -> new BusinessException("gestionnaireId", "Le gestionnaire assigné n'existe pas."));

        Client client = clientMapper.toEntity(clientDTO, gestionnaire);

        // Génération automatique d'un code client unique ISO/Standard d'entreprise
        client.setCodeClient("CLT-" + LocalDateTime.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase());

        Client savedClient = clientRepository.save(client);
        return clientMapper.toDto(savedClient);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientDTO getClientById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new BusinessException("id", "Client introuvable."));
        return clientMapper.toDto(client);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientDTO getClientByCode(String codeClient) {
        Client client = clientRepository.findByCodeClient(codeClient)
                .orElseThrow(() -> new BusinessException("codeClient", "Code client introuvable."));
        return clientMapper.toDto(client);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientDTO> getPaginatedClients(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Client> clientPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            clientPage = clientRepository.searchClients(keyword, pageable);
        } else {
            clientPage = clientRepository.findAll(pageable);
        }

        return clientPage.map(clientMapper::toDto);
    }
}
