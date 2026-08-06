package org.gimuemoa.minicbs.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.gimuemoa.minicbs.dto.BankAccountDTO;
import org.gimuemoa.minicbs.dto.ClientDTO;
import org.gimuemoa.minicbs.exceptions.CustomExceptions.BusinessException;
import org.gimuemoa.minicbs.model.enums.EnumAccountStatus;
import org.gimuemoa.minicbs.model.enums.EnumAccountType;
import org.gimuemoa.minicbs.service.BankAccountService;
import org.gimuemoa.minicbs.service.ClientService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountService accountService;
    private final ClientService clientService;

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("allTypes", Arrays.stream(EnumAccountType.values()).map(Enum::name).collect(Collectors.toList()));
        model.addAttribute("allStatus", Arrays.stream(EnumAccountStatus.values()).map(Enum::name).collect(Collectors.toList()));
    }

    @GetMapping
    public String listAccounts(
            @RequestParam(value = "search", required = false, defaultValue = "") String search,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "5") int size,
            @RequestHeader(value = "HX-Request", required = false) String htmxRequest,
            Model model) {

        // Appel de la méthode paginée propre et ultra-sécurisée
        Page<BankAccountDTO> accountPage = accountService.getPaginatedAccounts(search, page, size);

        model.addAttribute("accounts", accountPage.getContent());
        model.addAttribute("currentPageNumber", accountPage.getNumber());
        model.addAttribute("totalPages", accountPage.getTotalPages());
        model.addAttribute("totalElements", accountPage.getTotalElements());
        model.addAttribute("search", search);
        model.addAttribute("currentPage", "accounts");

        if (htmxRequest != null) {
            return "accounts/list :: account-table-content";
        }

        return "accounts/list";
    }

    // 2. Afficher le formulaire d'ouverture de compte
    @GetMapping("/new")
    public String showOpenAccountForm(Model model) {
        BankAccountDTO accountDTO = new BankAccountDTO();
        accountDTO.setCurrency("XOF"); // Devise officielle de l'UEMOA par défaut

        // On récupère tous les clients existants pour le menu déroulant du titulaire
        Page<ClientDTO> clientsPage = clientService.getPaginatedClients("", 0, 100);

        model.addAttribute("accountDTO", accountDTO);
        model.addAttribute("clientsList", clientsPage.getContent());
        return "accounts/form";
    }

    // 3. Traiter la demande d'ouverture (Génération du RIB GIM)
    @PostMapping("/save")
    public String openAccount(
            @Valid @ModelAttribute("accountDTO") BankAccountDTO accountDTO,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            Page<ClientDTO> clientsPage = clientService.getPaginatedClients("", 0, 100);
            model.addAttribute("clientsList", clientsPage.getContent());
            return "accounts/form";
        }

        try {
            accountService.openAccount(accountDTO);
            return "redirect:/accounts";
        } catch (BusinessException ex) {
            if (ex.getFieldName() != null) {
                result.rejectValue(ex.getFieldName(), "error.business", ex.getMessage());
            } else {
                model.addAttribute("globalErrorMessage", ex.getMessage());
            }
            Page<ClientDTO> clientsPage = clientService.getPaginatedClients("", 0, 100);
            model.addAttribute("clientsList", clientsPage.getContent());
            return "accounts/form";
        }
    }
}
