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

    private static final String CODE_BANQUE_GIM = "05401"; // Code banque fictif GIM zone UEMOA
    private static final String CODE_GUICHET_DEFAULT = "01001"; // Premier guichet principal
    private static final String CODE_PAYS_CI = "CI"; // Côte d'Ivoire par défaut (UEMOA format)

    @Override
    public BankAccountDTO openAccount(BankAccountDTO accountDTO) {
        // 1. Vérification de l'existence du client
        Client client = clientRepository.findById(accountDTO.getClientId())
                .orElseThrow(() -> new BusinessException("clientId", "Le client spécifié n'existe pas."));

        // 2. Génération automatique du numéro de compte conforme RIB UEMOA
        String generatedRib = generateUemoaRib(CODE_PAYS_CI, CODE_BANQUE_GIM, CODE_GUICHET_DEFAULT);

        // Sécurité anti-collision (au cas où le numéro aléatoire généré existerait déjà)
        while (accountRepository.existsByAccountNumber(generatedRib)) {
            generatedRib = generateUemoaRib(CODE_PAYS_CI, CODE_BANQUE_GIM, CODE_GUICHET_DEFAULT);
        }

        // 3. Transformation du DTO en Entité et affectation des valeurs d'ouverture
        BankAccount account = accountMapper.toEntity(accountDTO, client);
        account.setAccountNumber(generatedRib);
        account.setBalance(accountDTO.getBalance() != null ? accountDTO.getBalance() : BigDecimal.ZERO);
        account.setStatus(EnumAccountStatus.ACTIF);

        // 4. Sauvegarde dans le Grand Livre des comptes
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
        // Génération d'un numéro de compte séquentiel aléatoire de 11 chiffres
        Random random = new Random();
        long numCompteSeq = 10000000000L + (long)(random.nextDouble() * 90000000000L);
        String numCompteStr = String.valueOf(numCompteSeq);

        // Préparation du calcul mathématique à grande échelle (BigInteger requis)
        String bigIntString = banque + guichet + numCompteStr + "00";
        BigInteger textNumber = new BigInteger(bigIntString);
        BigInteger modulo97 = textNumber.mod(BigInteger.valueOf(97));

        int cleRibInt = 97 - modulo97.intValue();
        String cleRibStr = cleRibInt < 10 ? "0" + cleRibInt : String.valueOf(cleRibInt);

        // Format final Standardisé UEMOA : CI05401001011234567890145 (24 caractères)
        return pays + banque + guichet + numCompteStr + cleRibStr;
    }
}
