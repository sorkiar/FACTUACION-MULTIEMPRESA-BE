package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.ClientAddress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientAddressRepository extends JpaRepository<ClientAddress, Long> {

  List<ClientAddress> findByClientIdAndDeletedAtIsNull(Long clientId);

  Optional<ClientAddress> findByIdAndClientIdAndDeletedAtIsNull(Long id, Long clientId);
}
