package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.Profile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProfileRepository extends JpaRepository<Profile, Long>,
    JpaSpecificationExecutor<Profile> {

  Optional<Profile> findByIdAndCompany_IdAndIsSystemFalse(Long id, Long companyId);

  boolean existsByIdAndIsSystemTrue(Long id);

  @EntityGraph(attributePaths = {"menus"})
  List<Profile> findByIsSystemTrue();

  @Modifying
  @Query(value = "INSERT IGNORE INTO profile_menu (profile_id, menu_id) "
      + "SELECT id, :menuId FROM profile WHERE is_system = 1", nativeQuery = true)
  void assignMenuToAllSystemProfiles(@Param("menuId") Long menuId);
}
