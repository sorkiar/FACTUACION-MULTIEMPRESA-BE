package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.Profile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProfileRepository extends JpaRepository<Profile, Long>,
    JpaSpecificationExecutor<Profile> {

  Optional<Profile> findByIdAndCompany_IdAndIsSystemFalse(Long id, Long companyId);

  boolean existsByIdAndIsSystemTrue(Long id);

  @EntityGraph(attributePaths = {"menus"})
  List<Profile> findByIsSystemTrue();
}
