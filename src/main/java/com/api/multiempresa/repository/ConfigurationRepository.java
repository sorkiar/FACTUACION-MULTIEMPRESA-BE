package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.Configuration;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConfigurationRepository
    extends JpaRepository<Configuration, Long> {

  List<Configuration> findByConfigGroupAndDeletedAtIsNull(String configGroup);

  Optional<Configuration> findByConfigGroupAndConfigKeyAndDeletedAtIsNull(
      String configGroup, String configKey);

  List<Configuration> findByCompany_IdAndConfigGroupAndDeletedAtIsNull(
      Long companyId, String configGroup);

  Optional<Configuration> findByCompany_IdAndConfigGroupAndConfigKeyAndDeletedAtIsNull(
      Long companyId, String configGroup, String configKey);

  @Query("SELECT c FROM Configuration c WHERE c.editable = :editable AND c.deletedAt IS NULL ORDER BY c.sortOrder ASC, c.configGroup ASC, c.configKey ASC")
  List<Configuration> findEditableOrdered(@Param("editable") Integer editable);

  Optional<Configuration> findByIdAndDeletedAtIsNull(Long id);
}
