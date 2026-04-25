package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserRepository
    extends JpaRepository<User, Long>,
    JpaSpecificationExecutor<User> {

  @EntityGraph(attributePaths = {
      "documentType",
      "profile",
      "company",
  })
  Optional<User> findByUsername(String username);

  boolean existsByUsername(String username);

  boolean existsByUsernameAndCompanyId(String username, Long companyId);

  Optional<User> findByIdAndDeletedAtIsNull(Long id);
}
