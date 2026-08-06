package org.gimuemoa.minicbs.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.gimuemoa.minicbs.dto.AccountingEntryDTO;
import org.gimuemoa.minicbs.dto.BankTransactionDTO;
import org.gimuemoa.minicbs.exceptions.CustomExceptions.BusinessException;
import org.gimuemoa.minicbs.model.enums.EnumTransactionType;
import org.gimuemoa.minicbs.service.BankTransactionService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class BankTransactionController {

    private final BankTransactionService transactionService;

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("allTypes", Arrays.stream(EnumTransactionType.values()).map(Enum::name).collect(Collectors.toList()));
    }

    // Afficher le guichet de saisie
    @GetMapping("/guichet")
    public String showGuichet(Model model) {
        model.addAttribute("transactionDTO", new BankTransactionDTO());
        model.addAttribute("currentPage", "transactions");
        return "transactions/guichet";
    }

    // Traiter l'opération financière
    @PostMapping("/execute")
    public String executeTransaction(
            @Valid @ModelAttribute("transactionDTO") BankTransactionDTO dto,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            return "transactions/guichet";
        }

        try {
            transactionService.executeTransaction(dto);
            // En cas de succès, on redirige vers une page de succès ou vers le guichet avec un message
            model.addAttribute("successMessage", "L'opération financière a été validée avec succès dans le Grand Livre.");
            model.addAttribute("transactionDTO", new BankTransactionDTO()); // On vide le formulaire
            return "transactions/guichet";
        } catch (BusinessException ex) {
            if (ex.getFieldName() != null) {
                result.rejectValue(ex.getFieldName(), "error.business", ex.getMessage());
            } else {
                model.addAttribute("globalErrorMessage", ex.getMessage());
            }
            return "transactions/guichet";
        }
    }

    //Journal des opérations
    @GetMapping("/journal")
    public String showJournal(
            @RequestParam(value = "search", required = false, defaultValue = "") String search,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(value = "sortBy", required = false, defaultValue = "executedAt") String sortBy, // Par défaut trié par date
            @RequestParam(value = "direction", required = false, defaultValue = "desc") String direction, // Plus récent en premier
            @RequestHeader(value = "HX-Request", required = false) String htmxRequest,
            Model model) {

        // On passe l'ensemble des arguments de tri au service
        Page<BankTransactionDTO> journalPage = transactionService.getAllTransactionsJournal(search, page, size, sortBy, direction);

        model.addAttribute("transactions", journalPage.getContent());
        model.addAttribute("currentPageNumber", journalPage.getNumber());
        model.addAttribute("totalPages", journalPage.getTotalPages());
        model.addAttribute("totalElements", journalPage.getTotalElements());
        model.addAttribute("search", search);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        model.addAttribute("currentPage", "journal");

        if (htmxRequest != null) {
            return "transactions/journal :: journal-table-content";
        }

        return "transactions/journal";
    }


    // Import requis : org.gimuemoa.minicbs.dto.AccountingEntryDTO;

    @GetMapping("/grand-livre")
    public String showGeneralLedger(
            @RequestParam(value = "search", required = false, defaultValue = "") String search,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "15") int size, // 15 lignes d'écritures
            @RequestHeader(value = "HX-Request", required = false) String htmxRequest,
            Model model) {

        // Appel du service de comptabilité pure
        Page<AccountingEntryDTO> ledgerPage = transactionService.getGeneralLedger(search, page, size);

        model.addAttribute("entries", ledgerPage.getContent());
        model.addAttribute("currentPageNumber", ledgerPage.getNumber());
        model.addAttribute("totalPages", ledgerPage.getTotalPages());
        model.addAttribute("totalElements", ledgerPage.getTotalElements());
        model.addAttribute("search", search);
        model.addAttribute("currentPage", "grand-livre"); // ID unique pour l'onglet actif

        if (htmxRequest != null) {
            return "transactions/grand_livre :: ledger-table-content";
        }

        return "transactions/grand_livre";
    }


}
