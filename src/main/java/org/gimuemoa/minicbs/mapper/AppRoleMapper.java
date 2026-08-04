package org.gimuemoa.minicbs.mapper;

import org.gimuemoa.minicbs.dto.AppRoleDTO;
import org.gimuemoa.minicbs.model.AppRole;
import org.gimuemoa.minicbs.model.enums.EnumRole;
import org.springframework.stereotype.Component;

@Component
public class AppRoleMapper {
    public AppRoleDTO toDto(AppRole role) {
        if (role == null) {
            return null;
        }

        return AppRoleDTO.builder()
                .id(role.getId())
                .roleName(role.getRoleName() != null ? role.getRoleName().name() : null)
                .build();
    }

    public AppRole toEntity(AppRoleDTO dto) {
        return AppRole.builder()
                .id(dto.getId())
                .roleName(dto.getRoleName() != null ? EnumRole.valueOf(dto.getRoleName()) : null)
                .build();
    }
}

