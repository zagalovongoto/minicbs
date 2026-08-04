package org.gimuemoa.minicbs.service;

import org.gimuemoa.minicbs.dto.ClientDTO;
import org.springframework.data.domain.Page;

public interface ClientService {
    ClientDTO createClient(ClientDTO clientDTO);
    ClientDTO getClientById(Long id);
    ClientDTO getClientByCode(String codeClient);
    Page<ClientDTO> getPaginatedClients(String keyword, int page, int size);
}
