package org.gimuemoa.minicbs.config;

import lombok.RequiredArgsConstructor;
import org.gimuemoa.minicbs.config.properties.BankInitProperties;
import org.gimuemoa.minicbs.model.*;
import org.gimuemoa.minicbs.model.enums.EnumAccountStatus;
import org.gimuemoa.minicbs.model.enums.EnumAccountType;
import org.gimuemoa.minicbs.model.enums.EnumRole;
import org.gimuemoa.minicbs.model.enums.EnumStatut;
import org.gimuemoa.minicbs.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;

@Component
@RequiredArgsConstructor
public class BankInitializer implements CommandLineRunner {

    private final BankInitProperties initProperties;
    private final SystemParameterRepository parameterRepository;
    private final AppUserRepository userRepository;
    private final AppRoleRepository roleRepository; // Injection du repository des rôles
    private final ClientRepository clientRepository;
    private final BankAccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println(">>> Core Banking : Amorçage de la console d'administration...");

        // 1. Amorçage dynamique des paramètres dictionnaire système depuis le YAML
        if (parameterRepository.count() == 0) {
            initProperties.getParameters().forEach(p -> {
                parameterRepository.save(SystemParameter.builder()
                        .paramKey(p.getKey())
                        .paramValue(p.getValue())
                        .description(p.getDescription())
                        .isEditable(p.isEditable())
                        .build());
            });
            System.out.println(">>> Core Banking : Dictionnaire des constantes système injecté.");
        }

        // 2. NOUVEAU : Amorçage dynamique des rôles configurés dans le application.yml
        initProperties.getRoles().forEach(roleEnum -> {
            if (!roleRepository.existsByRoleName(EnumRole.valueOf(roleEnum))) {
                roleRepository.save(AppRole.builder()
                        .roleName(EnumRole.valueOf(roleEnum))
                        .build());
                System.out.println(">>> Core Banking : Habilitation [" + EnumRole.valueOf(roleEnum).name() + "] initialisée.");
            }
        });

        // 3. Amorçage du Super Administrateur souverain et liaison de son rôle
        if (userRepository.count() == 0) {
            BankInitProperties.AdminUser adminCfg = initProperties.getAdmin();
            // Récupération du rôle SUPER_ADMIN créé juste au-dessus
            AppRole superAdminRole = roleRepository.findByRoleName(EnumRole.ROLE_SUPER_ADMIN)
                    .orElseThrow(() -> new IllegalStateException("Erreur critique : Le rôle ROLE_SUPER_ADMIN n'a pas été initialisé."));

            AppUser superAdmin = AppUser.builder()
                    .nom(adminCfg.getNom())
                    .prenom(adminCfg.getPrenom())
                    .email(adminCfg.getEmail())
                    .telephone(adminCfg.getTelephone())
                    .statut(EnumStatut.ACTIF)
                    .password(passwordEncoder.encode(adminCfg.getPassword()))
                    .dateCreation(LocalDateTime.now())
                    .roles(new HashSet<>()) // Initialisation de la collection ManyToMany
                    .build();

            // Liaison de l'habilitation maximale au compte
            superAdmin.getRoles().add(superAdminRole);

            userRepository.save(superAdmin);
            System.out.println(">>> Core Banking : Utilisateur " + adminCfg.getEmail() + " créé avec le rôle " + superAdminRole.getRoleName().name() + ".");
        }

        // 4. Amorçage des comptes de trésorerie internes de l'institution
        initBankInternalAccounts();
    }

    private void initBankInternalAccounts() {
        String caisseNum = parameterRepository.findById("GL_ACCOUNT_CAISSE").map(SystemParameter::getParamValue).orElseThrow();
        String produitsNum = parameterRepository.findById("GL_ACCOUNT_PRODUITS").map(SystemParameter::getParamValue).orElseThrow();
        String capitalNum = parameterRepository.findById("GL_ACCOUNT_CAPITAL").map(SystemParameter::getParamValue).orElseThrow();
        String liaisonNum = parameterRepository.findById("GL_ACCOUNT_LIAISON").map(SystemParameter::getParamValue).orElseThrow();

        String clientSystemCode = "CLT-SYSTEM-BKN";
        Client bankClient = clientRepository.findByCodeClient(clientSystemCode).orElseGet(() -> {
            AppUser sysManager = userRepository.findAll().get(0);
            return clientRepository.save(Client.builder()
                    .codeClient(clientSystemCode)
                    .nom("GIM-BANK")
                    .prenom("TREASURY-HQ")
                    .email("treasury@gim-uemoa.org")
                    .telephone("+22527220000")
                    .gestionnaire(sysManager)
                    .build());
        });

        if (!accountRepository.existsByAccountNumber(caisseNum)) {
            accountRepository.save(BankAccount.builder().accountNumber(caisseNum).balance(BigDecimal.ZERO).currency("XOF").type(EnumAccountType.COURANT).status(EnumAccountStatus.ACTIF).client(bankClient).build());
            accountRepository.save(BankAccount.builder().accountNumber(produitsNum).balance(BigDecimal.ZERO).currency("XOF").type(EnumAccountType.COURANT).status(EnumAccountStatus.ACTIF).client(bankClient).build());
            accountRepository.save(BankAccount.builder().accountNumber(capitalNum).balance(new BigDecimal("1000000000")).currency("XOF").type(EnumAccountType.EPARGNE).status(EnumAccountStatus.ACTIF).client(bankClient).build());
            accountRepository.save(BankAccount.builder().accountNumber(liaisonNum).balance(new BigDecimal("500000000")).currency("XOF").type(EnumAccountType.COURANT).status(EnumAccountStatus.ACTIF).client(bankClient).build());

            System.out.println(">>> Core Banking : Les 4 comptes généraux de liaison interbancaire sont ouverts.");
        }
    }
}
