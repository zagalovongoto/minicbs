package org.gimuemoa.minicbs.repository;

import org.gimuemoa.minicbs.model.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    // Utile pour vérifier l'unicité de l'email lors de l'inscription
    Optional<AppUser> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM AppUser u WHERE " +
            "LOWER(u.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.prenom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<AppUser> searchUsers(@Param("keyword") String keyword, Pageable pageable);
}

