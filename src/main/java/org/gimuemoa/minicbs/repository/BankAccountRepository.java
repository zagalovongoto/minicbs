package org.gimuemoa.minicbs.repository;

import org.gimuemoa.minicbs.model.BankAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    Optional<BankAccount> findByAccountNumber(String accountNumber);
    boolean existsByAccountNumber(String accountNumber);
    List<BankAccount> findByClientId(Long clientId);
    // Nouvelle requête de recherche paginée globale (Base de données)
    @Query("SELECT a FROM BankAccount a WHERE " +
            "LOWER(a.accountNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.client.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.client.prenom) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<BankAccount> searchAccounts(@Param("keyword") String keyword, Pageable pageable);
}


