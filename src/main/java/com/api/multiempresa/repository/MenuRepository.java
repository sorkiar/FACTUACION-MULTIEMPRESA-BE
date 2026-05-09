package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.Menu;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MenuRepository extends JpaRepository<Menu, Long> {

  @Query("SELECT m FROM Menu m LEFT JOIN FETCH m.parent ORDER BY m.sortOrder ASC, m.id ASC")
  List<Menu> findAllWithParent();

  @Query(value = "SELECT menu_id FROM profile_menu WHERE profile_id = :profileId",
      nativeQuery = true)
  List<Long> findMenuIdsByProfileId(
      @org.springframework.data.repository.query.Param("profileId") Long profileId);

  boolean existsByName(String name);

  boolean existsByNameAndIdNot(String name, Long id);
}
