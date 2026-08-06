package org.gimuemoa.minicbs.service.impl;

import lombok.RequiredArgsConstructor;
import org.gimuemoa.minicbs.dto.BankAccountDTO;
import org.gimuemoa.minicbs.exceptions.CustomExceptions.BusinessException;
import org.gimuemoa.minicbs.mapper.BankAccountMapper;
import org.gimuemoa.minicbs.model.BankAccount;
import org.gimuemoa.minicbs.model.Client;
import org.gimuemoa.minicbs.model.enums.EnumAccountStatus;
import org.gimuemoa.minicbs.repository.BankAccountRepository;
import org.gimuemoa.minicbs.repository.ClientRepository;
import org.gimuemoa.minicbs.service.BankAccountService;
import org.gimuemoa.minicbs.service.SystemParameterService; // INJECTION
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountRepository accountRepository;
    private final ClientRepository clientRepository;
    private final BankAccountMapper accountMapper;
    private final SystemParameterService paramService; // SÉCURITÉ : Service dictionnaire injecté

    private static final String CODE_PAYS_CI = "CI"; // Reste fixe pour le format d'arborescence

    @Override
    public BankAccountDTO openAccount(BankAccountDTO accountDTO) {
        // 1. Vérification de l'existence du client
        Client client = clientRepository.findById(accountDTO.getClientId())
                .orElseThrow(() -> new BusinessException("clientId", "Le client spécifié n'existe pas."));

        // 2. EXTRACTION COMPTABLE DYNAMIQUE DE LA STRUCTURE DE L'IBAN DEPUIS LA BASE SQL
        String codeEtab = paramService.getRequiredString("BANK_CODE_ETAB");       // Récupère ex: "CI054" -> on extrait le code numérique
        String codeGuichet = paramService.getRequiredString("BANK_CODE_GUICHET"); // Récupère ex: "01001"

        // Ajustement prudentiel : Nettoyer le code pays s'il est déjà inclus dans le paramètre de la base
        String banqueClean = codeEtab.replace(CODE_PAYS_CI, "").trim(); // Si "CI054", isole "054"

        // 3. Génération automatique du numéro de compte conforme RIB UEMOA
        String generatedRib = generateUemoaRib(CODE_PAYS_CI, banqueClean, codeGuichet);

        // Sécurité anti-collision
        while (accountRepository.existsByAccountNumber(generatedRib)) {
            generatedRib = generateUemoaRib(CODE_PAYS_CI, banqueClean, codeGuichet);
        }

        // 4. Transformation du DTO en Entité et affectation des valeurs d'ouverture
        BankAccount account = accountMapper.toEntity(accountDTO, client);
        account.setAccountNumber(generatedRib);
        account.setBalance(accountDTO.getBalance() != null ? accountDTO.getBalance() : BigDecimal.ZERO);
        account.setStatus(EnumAccountStatus.ACTIF);

        BankAccount savedAccount = accountRepository.save(account);
        return accountMapper.toDto(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public BankAccountDTO getAccountByNumber(String accountNumber) {
        BankAccount account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BusinessException("accountNumber", "Compte bancaire introuvable."));
        return accountMapper.toDto(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankAccountDTO> getAccountsByClient(Long clientId) {
        return accountRepository.findByClientId(clientId).stream()
                .map(accountMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void updateAccountStatus(Long id, String status) {
        BankAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new BusinessException("status", "Compte introuvable."));
        account.setStatus(EnumAccountStatus.valueOf(status));
        accountRepository.save(account);
    }

    /**
     * Algorithme Officiel de calcul de Clé RIB - Norme BCEAO / UEMOA
     * Formule : 97 - (( (Banque * 10^5 + Guichet) * 10^11 + NumCompte ) * 10^2) % 97
     */
    private String generateUemoaRib(String pays, String banque, String guichet) {
        // Assure que le code banque fait 5 caractères pour l'UEMOA (ex: "05401")
        String banqueUemoa = banque.length() == 3 ? banque + "01" : banque;

        // Génération d'un numéro de compte séquentiel aléatoire de 11 chiffres
        Random random = new Random();
        long numCompteSeq = 10000000000L + (long)(random.nextDouble() * 90000000000L);
        String numCompteStr = String.valueOf(numCompteSeq);

        // Préparation du calcul mathématique à grande échelle (BigInteger requis)
        String bigIntString = banqueUemoa + guichet + numCompteStr + "00";
        BigInteger textNumber = new BigInteger(bigIntString);
        BigInteger modulo97 = textNumber.mod(BigInteger.valueOf(97));

        int cleRibInt = 97 - modulo97.intValue();
        String cleRibStr = cleRibInt < 10 ? "0" + cleRibInt : String.valueOf(cleRibInt);

        // Format final Standardisé UEMOA : CI + 5 chf Banque + 5 chf Guichet + 11 chf Num + 2 chf Clé
        return pays + banqueUemoa + guichet + numCompteStr + cleRibStr;
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<BankAccountDTO> getPaginatedAccounts(String keyword, int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                page, size, org.springframework.data.domain.Sort.by("id").descending()
        );

        org.springframework.data.domain.Page<BankAccount> accountPage = (keyword != null && !keyword.trim().isEmpty()) ?
                accountRepository.searchAccounts(keyword.trim(), pageable) : accountRepository.findAll(pageable);

        return accountPage.map(accountMapper::toDto);
    }
}
