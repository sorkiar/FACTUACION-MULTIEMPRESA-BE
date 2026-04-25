package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.RemissionGuideDriver;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RemissionGuideDriverRepository extends JpaRepository<RemissionGuideDriver, Long> {

  @EntityGraph(attributePaths = {"driver", "driverVehicle"})
  List<RemissionGuideDriver> findByRemissionGuideIdAndDeletedAtIsNull(Long remissionGuideId);
}
