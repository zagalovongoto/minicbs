package org.gimuemoa.minicbs.repository;

import org.gimuemoa.minicbs.model.AccountingEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountingEntryRepository extends JpaRepository<AccountingEntry, Long> {

    @Query("SELECT e FROM AccountingEntry e WHERE " +
            "LOWER(e.accountNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.transaction.reference) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<AccountingEntry> searchInLedger(@Param("keyword") String keyword, Pageable pageable);
}
