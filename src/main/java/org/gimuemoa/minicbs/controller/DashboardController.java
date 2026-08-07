package org.gimuemoa.minicbs.controller;

import lombok.RequiredArgsConstructor;
import org.gimuemoa.minicbs.model.BankAccount;
import org.gimuemoa.minicbs.repository.BankAccountRepository;
import org.gimuemoa.minicbs.repository.ClientRepository;
import org.gimuemoa.minicbs.repository.BankTransactionRepository;
import org.gimuemoa.minicbs.service.SystemParameterService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final BankAccountRepository accountRepository;
    private final ClientRepository clientRepository;
    private final BankTransactionRepository transactionRepository;
    private final SystemParameterService paramService;

    @GetMapping("/")
    public String showDashboard(Model model) {
        // 1. Extraction dynamique des coordonnées des comptes de structure depuis la base SQL
        String caisseNum = paramService.getRequiredString("GL_ACCOUNT_CAISSE");
        String produitsNum = paramService.getRequiredString("GL_ACCOUNT_PRODUITS");
        String liaisonNum = paramService.getRequiredString("GL_ACCOUNT_LIAISON");

        // 2. Récupération sécurisée des soldes de trésorerie en temps réel
        BigDecimal soldeCaisse = accountRepository.findByAccountNumber(caisseNum)
                .map(BankAccount::getBalance).orElse(BigDecimal.ZERO);
        BigDecimal soldeProduits = accountRepository.findByAccountNumber(produitsNum)
                .map(BankAccount::getBalance).orElse(BigDecimal.ZERO);
        BigDecimal soldeLiaison = accountRepository.findByAccountNumber(liaisonNum)
                .map(BankAccount::getBalance).orElse(BigDecimal.ZERO);

        // 3. Calcul des indicateurs de volume opérationnel
        long totalClients = clientRepository.count();
        long totalAccounts = accountRepository.count();
        long totalTransactions = transactionRepository.count();

        // 4. Injection des variables dans le modèle Thymeleaf
        model.addAttribute("soldeCaisse", soldeCaisse);
        model.addAttribute("soldeProduits", soldeProduits);
        model.addAttribute("soldeLiaison", soldeLiaison);
        model.addAttribute("totalClients", totalClients);
        model.addAttribute("totalAccounts", totalAccounts);
        model.addAttribute("totalTransactions", totalTransactions);

        // Alimentation de l'historique flash (les 5 dernières écritures du grand livre)
        model.addAttribute("latestTransactions", transactionRepository.findAll(
                org.springframework.data.domain.PageRequest.of(0, 5,
                        org.springframework.data.domain.Sort.by("executedAt").descending())).getContent());

        model.addAttribute("currentPage", "dashboard"); // Allume l'onglet d'accueil dans la sidebar
        return "dashboard"; // Redirige vers templates/dashboard.html
    }
}
