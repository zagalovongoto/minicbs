package org.gimuemoa.minicbs.service;

import org.gimuemoa.minicbs.dto.AppRoleDTO;
import java.util.List;

public interface AppRoleService {
    AppRoleDTO createRole(AppRoleDTO roleDTO);
    List<AppRoleDTO> getAllRoles();
    AppRoleDTO getRoleById(Long id);
    void deleteRole(Long id);
}

