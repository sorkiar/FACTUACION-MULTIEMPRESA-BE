package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.Company;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

  Optional<Company> findByRuc(String ruc);

  boolean existsByRucAndDeletedAtIsNull(String ruc);

  boolean existsByRucAndDeletedAtIsNullAndIdNot(String ruc, Long id);

  List<Company> findByDeletedAtIsNull();

  Optional<Company> findByIdAndDeletedAtIsNull(Long id);
}
