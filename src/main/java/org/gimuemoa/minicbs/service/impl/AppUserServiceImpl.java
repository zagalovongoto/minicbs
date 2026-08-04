package org.gimuemoa.minicbs.service.impl;

import lombok.RequiredArgsConstructor;
import org.gimuemoa.minicbs.dto.AppUserDTO;
import org.gimuemoa.minicbs.exceptions.CustomExceptions.BusinessException;
import org.gimuemoa.minicbs.mapper.AppUserMapper;
import org.gimuemoa.minicbs.model.AppRole;
import org.gimuemoa.minicbs.model.AppUser;
import org.gimuemoa.minicbs.model.enums.EnumRole;
import org.gimuemoa.minicbs.model.enums.EnumStatut;
import org.gimuemoa.minicbs.repository.AppRoleRepository;
import org.gimuemoa.minicbs.repository.AppUserRepository;
import org.gimuemoa.minicbs.service.AppUserService;
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

    @Override
    public AppUserDTO createUser(AppUserDTO userDTO) {
        // 1. Vérification de l'unicité de l'email
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new BusinessException("email", "Cette adresse email est déjà utilisée.");
        }

        // 2. Conversion partielle du DTO vers l'entité
        AppUser user = userMapper.toEntity(userDTO);

        // 3. Récupération des VRAIS rôles depuis la base de données
        Set<AppRole> databaseRoles = new HashSet<>();
        if (userDTO.getRoles() != null) {
            for (String roleNameStr : userDTO.getRoles()) {
                EnumRole enumRole = EnumRole.valueOf(roleNameStr);
                AppRole appRole = roleRepository.findByRoleName(enumRole)
                        .orElseThrow(() -> new RuntimeException("Le rôle " + roleNameStr + " n'existe pas en base de données."));
                databaseRoles.add(appRole);
            }
        }

        // On associe les rôles gérés par Hibernate à notre entité
        user.setRoles(databaseRoles);

        // 4. Sauvegarde
        AppUser savedUser = userRepository.save(user);
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
                .orElseThrow(() -> new RuntimeException("Modification impossible, utilisateur introuvable."));

        // Mise à jour des informations de base
        existingUser.setNom(userDTO.getNom());
        existingUser.setPrenom(userDTO.getPrenom());
        existingUser.setEmail(userDTO.getEmail());
        existingUser.setTelephone(userDTO.getTelephone());
        existingUser.setPhoto(userDTO.getPhoto());
        existingUser.setStatut(userDTO.getStatut() != null ? EnumStatut.valueOf(userDTO.getStatut()) : null);

        // Mise à jour des rôles
        Set<AppRole> databaseRoles = new HashSet<>();
        if (userDTO.getRoles() != null) {
            for (String roleNameStr : userDTO.getRoles()) {
                EnumRole enumRole = EnumRole.valueOf(roleNameStr);
                AppRole appRole = roleRepository.findByRoleName(enumRole)
                        .orElseThrow(() -> new RuntimeException("Le rôle " + roleNameStr + " n'existe pas."));
                databaseRoles.add(appRole);
            }
        }
        existingUser.setRoles(databaseRoles);

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
    public org.springframework.data.domain.Page<AppUserDTO> getPaginatedAndSearchedUsers(
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
