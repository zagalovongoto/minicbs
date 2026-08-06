package org.gimuemoa.minicbs.repository;

import org.gimuemoa.minicbs.model.AppRole;
import org.gimuemoa.minicbs.model.enums.EnumRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AppRoleRepository extends JpaRepository<AppRole, Long> {
    Optional<AppRole> findByRoleName(EnumRole roleName);
    boolean existsByRoleName(EnumRole roleName);
}
