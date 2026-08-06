package org.gimuemoa.minicbs.repository;

import org.gimuemoa.minicbs.model.SystemParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SystemParameterRepository extends JpaRepository<SystemParameter, String> {
    Optional<SystemParameter> findByParamKey(String paramKey);
}
