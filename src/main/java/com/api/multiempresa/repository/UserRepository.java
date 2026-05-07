package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserRepository
    extends JpaRepository<User, Long>,
    JpaSpecificationExecutor<User> {

  @EntityGraph(attributePaths = {"documentType", "profile", "company"})
  Optional<User> findByUsername(String username);

  /** Used for login: find user by username + company RUC (multi-tenant). */
  @EntityGraph(attributePaths = {"documentType", "profile", "company"})
  Optional<User> findByUsernameAndCompany_Ruc(String username, String ruc);

  /** Used for sidebar/paths: same lookup but eager-loads profile.menus. */
  @EntityGraph(attributePaths = {"profile", "profile.menus", "company"})
  Optional<User> findWithMenusByUsernameAndCompany_Ruc(String username, String ruc);

  /** Legacy method kept for backward compatibility (single-tenant paths). */
  @EntityGraph(attributePaths = {"profile", "profile.menus"})
  Optional<User> findWithMenusByUsername(String username);

  boolean existsByUsername(String username);

  boolean existsByUsernameAndCompanyId(String username, Long companyId);

  Optional<User> findByIdAndDeletedAtIsNull(Long id);
}
