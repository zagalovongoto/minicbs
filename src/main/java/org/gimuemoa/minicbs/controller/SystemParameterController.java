package org.gimuemoa.minicbs.controller;

import lombok.RequiredArgsConstructor;
import org.gimuemoa.minicbs.model.SystemParameter;
import org.gimuemoa.minicbs.repository.SystemParameterRepository;
import org.gimuemoa.minicbs.service.SystemParameterService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SystemParameterController {

    private final SystemParameterRepository parameterRepository;
    private final SystemParameterService parameterService;

    // Afficher la console des paramètres système
    @GetMapping
    public String showSettingsConsole(Model model) {
        model.addAttribute("parameters", parameterRepository.findAll());
        model.addAttribute("currentPage", "settings"); // Active l'onglet dans la sidebar
        return "settings/console";
    }

    // Mettre à jour un paramètre en direct (AJAX via HTMX)
    @PostMapping("/update/{key}")
    public String updateParameterInline(
            @PathVariable("key") String key,
            @RequestParam("value") String value,
            Model model) {

        try {
            // Mise à jour via le service (vérifie si le champ est éditable)
            parameterService.updateParameter(key, value);
            model.addAttribute("successMessage", "Le paramètre " + key + " a été mis à jour avec succès dans le dictionnaire.");
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }

        // On rafraîchit la liste pour renvoyer le fragment mis à jour
        model.addAttribute("parameters", parameterRepository.findAll());
        return "settings/console :: parameters-list";
    }
}
