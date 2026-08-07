package org.gimuemoa.minicbs.service.impl;

import lombok.RequiredArgsConstructor;
import org.gimuemoa.minicbs.dto.AppUserDTO;
import org.gimuemoa.minicbs.exceptions.CustomExceptions.BusinessException;
import org.gimuemoa.minicbs.mapper.AppUserMapper;
import org.gimuemoa.minicbs.model.ActivationToken;
import org.gimuemoa.minicbs.model.AppRole;
import org.gimuemoa.minicbs.model.AppUser;
import org.gimuemoa.minicbs.model.enums.EnumRole;
import org.gimuemoa.minicbs.model.enums.EnumStatut;
import org.gimuemoa.minicbs.repository.ActivationTokenRepository;
import org.gimuemoa.minicbs.repository.AppRoleRepository;
import org.gimuemoa.minicbs.repository.AppUserRepository;
import org.gimuemoa.minicbs.service.AppUserService;
import org.gimuemoa.minicbs.service.EmailService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository userRepository;
    private final AppRoleRepository roleRepository;
    private final AppUserMapper userMapper;
    private final ActivationTokenRepository tokenRepository;
    private final EmailService emailService;

    @Override
    public AppUserDTO createUser(AppUserDTO userDTO) {
        // 1. CONTRÔLES PRUDENTIELS D'UNICITÉ (BCEAO Standards)
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new BusinessException("email", "Cette adresse email professionnelle est deja utilisee.");
        }

        // Génération automatique du username si non fourni (ex: prenom.nom)
        String generatedUsername = userDTO.getUsername() != null ? userDTO.getUsername().trim().toLowerCase() :
                (userDTO.getPrenom().trim() + "." + userDTO.getNom().trim()).toLowerCase().replaceAll("\\s+", "");

        if (userRepository.existsByUsername(generatedUsername)) {
            throw new BusinessException("username", "L'identifiant de connexion [" + generatedUsername + "] est deja attribue à un autre agent.");
        }

        // 2. Conversion et préparation de l'entité
        AppUser user = userMapper.toEntity(userDTO);
        user.setUsername(generatedUsername);

        // Règle de sécurité : Un mot de passe aléatoire temporaire est injecté en attendant l'activation
        user.setPassword(java.util.UUID.randomUUID().toString());
        user.setMustChangePassword(true); // Bloquera l'accès via le filtre tant qu'il n'a pas défini sa clé
        user.setStatut(org.gimuemoa.minicbs.model.enums.EnumStatut.ACTIF); // Compte prêt à être activé

        // 3. Récupération des rôles depuis la base de données
        Set<AppRole> databaseRoles = new HashSet<>();
        if (userDTO.getRoles() != null) {
            for (String roleNameStr : userDTO.getRoles()) {
                EnumRole enumRole = EnumRole.valueOf(roleNameStr);
                AppRole appRole = roleRepository.findByRoleName(enumRole)
                        .orElseThrow(() -> new BusinessException("roles", "Le role " + roleNameStr + " n'existe pas en base."));
                databaseRoles.add(appRole);
            }
        }
        user.setRoles(databaseRoles);

        // 4. Sauvegarde de l'utilisateur dans le grand livre des habilitations
        AppUser savedUser = userRepository.save(user);

        // ==========================================================================
        // CRÉATION DU JETON D'ACTIVATION ET EXPÉDITION DU LIEN SÉCURISÉ (ÉTAPE 2)
        // ==========================================================================
        String uniqueToken = java.util.UUID.randomUUID().toString();

        ActivationToken activationToken = ActivationToken.builder()
                .token(uniqueToken)
                .user(savedUser)
                .expiryDate(java.time.LocalDateTime.now().plusDays(1)) // Expire strictement sous 24h
                .isUsed(false)
                .build();

        tokenRepository.save(activationToken);

        // Expédition asynchrone du mail d'activation aux couleurs du GIM
        emailService.sendActivationEmail(savedUser, uniqueToken);

        return userMapper.toDto(savedUser);
    }


    @Override
    @Transactional(readOnly = true)
    public List<AppUserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AppUserDTO getUserById(Long id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable avec l'ID: " + id));
        return userMapper.toDto(user);
    }

    @Override
    public AppUserDTO updateUser(Long id, AppUserDTO userDTO) {
        AppUser existingUser = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("id", "Modification impossible : Utilisateur introuvable."));

        // 1. CONTRÔLE PRUDENTIEL : Unicité de l'email si modifié
        if (!existingUser.getEmail().equalsIgnoreCase(userDTO.getEmail())) {
            if (userRepository.existsByEmail(userDTO.getEmail())) {
                throw new BusinessException("email", "Cette adresse email est déjà attribuée à un autre agent.");
            }
            existingUser.setEmail(userDTO.getEmail().trim().toLowerCase());
        }

        // 2. CONTRÔLE PRUDENTIEL : Unicité du username si modifié
        String newUsername = userDTO.getUsername() != null ? userDTO.getUsername().trim().toLowerCase() : existingUser.getUsername();
        if (!existingUser.getUsername().equalsIgnoreCase(newUsername)) {
            if (userRepository.existsByUsername(newUsername)) {
                throw new BusinessException("username", "L'identifiant [" + newUsername + "] est déjà utilisé par un autre agent.");
            }
            existingUser.setUsername(newUsername);
        }

        // 3. Mise à jour des informations de base
        existingUser.setNom(userDTO.getNom().trim());
        existingUser.setPrenom(userDTO.getPrenom().trim());
        existingUser.setTelephone(userDTO.getTelephone().trim());

        // Sécurité de statut : Si non fourni, on conserve l'ancien statut existant au lieu de mettre null
        if (userDTO.getStatut() != null) {
            existingUser.setStatut(EnumStatut.valueOf(userDTO.getStatut()));
        }

        // 4. Mise à jour des rôles d'habilitations
        Set<AppRole> databaseRoles = new HashSet<>();
        if (userDTO.getRoles() != null) {
            for (String roleNameStr : userDTO.getRoles()) {
                EnumRole enumRole = EnumRole.valueOf(roleNameStr);
                AppRole appRole = roleRepository.findByRoleName(enumRole)
                        .orElseThrow(() -> new BusinessException("roles", "Le rôle " + roleNameStr + " n'existe pas en base."));
                databaseRoles.add(appRole);
            }
            existingUser.setRoles(databaseRoles);
        }

        // Le déclencheur @PreUpdate onUpdate() de l'entité va automatiquement actualiser 'dateModification' ici !
        AppUser updatedUser = userRepository.save(existingUser);
        return userMapper.toDto(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Impossible de supprimer, utilisateur introuvable.");
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppUserDTO> getPaginatedAndSearchedUsers(
            String keyword, int page, int size, String sortBy, String direction) {

        // 1. Création de l'objet de tri et de pagination
        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);

        // 2. Appel du repository (recherche ou affichage global si le mot-clé est vide)
        Page<AppUser> userPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            userPage = userRepository.searchUsers(keyword, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        // 3. Conversion de la page d'Entités en page de DTOs
        return userPage.map(userMapper::toDto);
    }

}
