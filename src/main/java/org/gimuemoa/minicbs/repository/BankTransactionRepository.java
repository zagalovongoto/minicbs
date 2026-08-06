package org.gimuemoa.minicbs.repository;

import org.gimuemoa.minicbs.model.BankTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {
    Optional<BankTransaction> findByReference(String reference);

    @Query("SELECT t FROM BankTransaction t WHERE " +
            "t.sourceAccount.accountNumber = :accNum OR " +
            "t.destinationAccount.accountNumber = :accNum")
    Page<BankTransaction> findHistoryByAccountNumber(@Param("accNum") String accountNumber, Pageable pageable);

    @Query("SELECT t FROM BankTransaction t WHERE " +
            "LOWER(t.reference) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.sourceAccount.accountNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.destinationAccount.accountNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<BankTransaction> searchInJournal(@Param("keyword") String keyword, Pageable pageable);

}
