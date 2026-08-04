package org.gimuemoa.minicbs.repository;

import org.gimuemoa.minicbs.model.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByCodeClient(String codeClient);
    boolean existsByEmail(String email);

    @Query("SELECT c FROM Client c WHERE " +
            "LOWER(c.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.prenom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.codeClient) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Client> searchClients(@Param("keyword") String keyword, Pageable pageable);
}
