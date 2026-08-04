package org.gimuemoa.minicbs.service.impl;

import lombok.RequiredArgsConstructor;
import org.gimuemoa.minicbs.dto.AppRoleDTO;
import org.gimuemoa.minicbs.mapper.AppRoleMapper;
import org.gimuemoa.minicbs.model.AppRole;
import org.gimuemoa.minicbs.repository.AppRoleRepository;
import org.gimuemoa.minicbs.service.AppRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AppRoleServiceImpl implements AppRoleService {

    private final AppRoleRepository roleRepository;
    private final AppRoleMapper roleMapper;

    @Override
    public AppRoleDTO createRole(AppRoleDTO roleDTO) {
        AppRole role = roleMapper.toEntity(roleDTO);
        AppRole savedRole = roleRepository.save(role);
        return roleMapper.toDto(savedRole);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppRoleDTO> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AppRoleDTO getRoleById(Long id) {
        AppRole role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rôle introuvable avec l'ID: " + id));
        return roleMapper.toDto(role);
    }

    @Override
    public void deleteRole(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new RuntimeException("Impossible de supprimer, rôle introuvable.");
        }
        roleRepository.deleteById(id);
    }
}

