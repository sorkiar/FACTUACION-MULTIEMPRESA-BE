package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.PaymentMethod;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PaymentMethodRepository
    extends JpaRepository<PaymentMethod, Integer>,
    JpaSpecificationExecutor<PaymentMethod> {

  boolean existsByCode(String code);

  Optional<PaymentMethod> findByIdAndStatusNot(Integer id, Integer status);
}
