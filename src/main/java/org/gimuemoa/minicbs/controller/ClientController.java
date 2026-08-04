package org.gimuemoa.minicbs.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.gimuemoa.minicbs.dto.ClientDTO;
import org.gimuemoa.minicbs.exceptions.CustomExceptions.BusinessException;
import org.gimuemoa.minicbs.service.ClientService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    // Liste des clients (Compatible Recherche & Pagination HTMX)
    @GetMapping
    public String listClients(
            @RequestParam(value = "search", required = false, defaultValue = "") String search,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "5") int size,
            @RequestHeader(value = "HX-Request", required = false) String htmxRequest,
            Model model) {

        Page<ClientDTO> clientPage = clientService.getPaginatedClients(search, page, size);

        model.addAttribute("clients", clientPage.getContent());
        model.addAttribute("currentPageNumber", clientPage.getNumber());
        model.addAttribute("totalPages", clientPage.getTotalPages());
        model.addAttribute("totalElements", clientPage.getTotalElements());
        model.addAttribute("search", search);
        model.addAttribute("currentPage", "clients"); // Pour l'activation du menu sidebar

        if (htmxRequest != null) {
            return "clients/list :: client-table-content";
        }

        return "clients/list";
    }

    // Formulaire de création d'un client
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setGestionnaireId(1L); // Simulation du gestionnaire n°1 en attendant Spring Security
        model.addAttribute("clientDTO", clientDTO);
        return "clients/form";
    }

    // Sauvegarde du client
    @PostMapping("/save")
    public String saveClient(
            @Valid @ModelAttribute("clientDTO") ClientDTO clientDTO,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            return "clients/form";
        }

        try {
            clientService.createClient(clientDTO);
            return "redirect:/clients";
        } catch (BusinessException ex) {
            if (ex.getFieldName() != null) {
                result.rejectValue(ex.getFieldName(), "error.business", ex.getMessage());
            } else {
                model.addAttribute("globalErrorMessage", ex.getMessage());
            }
            return "clients/form";
        }
    }

    @GetMapping("/search-mini")
    public String searchMiniClients(
            @RequestParam(value = "q", required = false, defaultValue = "") String query,
            Model model) {

        // On réutilise votre service existant pour chercher les 5 meilleurs résultats
        Page<ClientDTO> clientPage = clientService.getPaginatedClients(query, 0, 5);
        model.addAttribute("suggestions", clientPage.getContent());

        return "accounts/form :: clients-suggestions"; // Renvoie uniquement le fragment de suggestions
    }

}
