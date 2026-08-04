package org.gimuemoa.minicbs.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.gimuemoa.minicbs.dto.BankTransactionDTO;
import org.gimuemoa.minicbs.exceptions.CustomExceptions.BusinessException;
import org.gimuemoa.minicbs.model.enums.EnumTransactionType;
import org.gimuemoa.minicbs.service.BankTransactionService;
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
}
