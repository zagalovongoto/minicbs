package org.gimuemoa.minicbs.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.gimuemoa.minicbs.dto.AppUserDTO;
import org.gimuemoa.minicbs.exceptions.CustomExceptions.BusinessException;
import org.gimuemoa.minicbs.model.enums.EnumRole;
import org.gimuemoa.minicbs.model.enums.EnumStatut;
import org.gimuemoa.minicbs.service.AppUserService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

        import java.util.Arrays;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class AppUserController {

    private final AppUserService userService;

    // Helper pour injecter la liste des enums dans les formulaires HTML
    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("allStatuts", Arrays.stream(EnumStatut.values()).map(Enum::name).collect(Collectors.toList()));
        model.addAttribute("allRoles", Arrays.stream(EnumRole.values()).map(Enum::name).collect(Collectors.toList()));
    }

    // 1. READ ALL : Liste des utilisateurs
    /*@GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("currentPage", "users");
        return "users/list"; // Fichier templates/users/list.html
    }*/

    @GetMapping
    public String listUsers(
            @RequestParam(value = "search", required = false, defaultValue = "") String search,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "6") int size, // 5 utilisateurs par page par exemple
            @RequestParam(value = "sortBy", required = false, defaultValue = "nom") String sortBy,
            @RequestParam(value = "direction", required = false, defaultValue = "asc") String direction,
            @RequestHeader(value = "HX-Request", required = false) String htmxRequest,
            Model model) {

        // 1. Récupération des données paginées
        Page<AppUserDTO> userPage = userService.getPaginatedAndSearchedUsers(search, page, size, sortBy, direction);

        // 2. Injection des variables dans le modèle Thymeleaf
        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPageNumber", userPage.getNumber());
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalElements", userPage.getTotalElements());
        model.addAttribute("search", search);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        model.addAttribute("currentPage", "users"); // Pour le menu sidebar actif

        // 3. LA MAGIE HTMX : Si la requête vient d'HTMX, on ne renvoie que le fragment des lignes
        if (htmxRequest != null) {
            return "users/list :: table-content"; // Renvoie uniquement le fragment défini dans list.html
        }

        // Sinon, chargement initial classique de toute la page
        return "users/list";
    }

    // 2. CREATE : Afficher le formulaire d'ajout
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("userDTO", new AppUserDTO());
        return "users/form"; // Fichier templates/users/form.html
    }

    // 3. CREATE/UPDATE : Enregistrer les données (avec gestion des erreurs de validation)
    @PostMapping("/save")
    public String saveUser(
            @Valid @ModelAttribute("userDTO") AppUserDTO userDTO,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            return "users/form"; // Reste sur la page en cas d'erreur de saisie
        }

        try {
            if (userDTO.getId() == null) {
                userService.createUser(userDTO);
            } else {
                userService.updateUser(userDTO.getId(), userDTO);
            }
            return "redirect:/users";
        }
        catch (BusinessException ex) {

            // Si l'erreur cible un champ précis, on l'associe directement à ce champ dans Thymeleaf
            if (ex.getFieldName() != null) {
                result.rejectValue(ex.getFieldName(), "error.business", ex.getMessage());
            } else {
                // Sinon, on l'affiche dans l'alerte globale au-dessus du formulaire
                model.addAttribute("globalErrorMessage", ex.getMessage());
            }

            return "users/form";
        }

    }

    // 4. UPDATE : Afficher le formulaire de modification pré-rempli
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        AppUserDTO userDTO = userService.getUserById(id);
        model.addAttribute("userDTO", userDTO);
        return "users/form";
    }

    // 5. DELETE CLASSIQUE : Conservé pour la rétrocompatibilité
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/users";
    }

    // 5b. DELETE HTMX : Nouvelle méthode dédiée à l'Itération 3
    @DeleteMapping("/delete/{id}")
    @ResponseBody // Indique à Spring de renvoyer directement le corps (ici du vide) au lieu d'une vue HTML
    public String deleteUserHtmx(@PathVariable Long id) {
        // Suppression en base de données via votre service existant
        userService.deleteUser(id);

        // On retourne une chaîne vide. HTMX va écraser la ligne ciblée par du vide, ce qui l'efface.
        return "";
    }
}

