package org.gimuemoa.minicbs.repository;

import org.gimuemoa.minicbs.model.AppRole;
import org.gimuemoa.minicbs.model.enums.EnumRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppRoleRepository extends JpaRepository<AppRole, Long> {
    // Permet de retrouver un rôle en base de données grâce à son énumération
    Optional<AppRole> findByRoleName(EnumRole roleName);
}

