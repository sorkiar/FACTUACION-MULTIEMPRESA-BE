package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.Carrier;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CarrierRepository
    extends JpaRepository<Carrier, Long>,
    JpaSpecificationExecutor<Carrier> {

  Optional<Carrier> findByIdAndCompany_IdAndStatusNot(Long id, Long companyId, Integer status);

  boolean existsByDocTypeAndDocNumberAndCompany_IdAndStatusNot(
      String docType, String docNumber, Long companyId, Integer status);

  boolean existsByDocTypeAndDocNumberAndCompany_IdAndStatusNotAndIdNot(
      String docType, String docNumber, Long companyId, Integer status, Long id);
}
