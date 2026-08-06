package org.gimuemoa.minicbs.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout,
                                Model model) {
        if (error != null) {
            model.addAttribute("loginError", "Identifiants invalides ou privilèges insuffisants.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "Déconnexion réussie de la console centrale.");
        }
        return "login"; // Va chercher obligatoirement src/main/resources/templates/login.html
    }
}
