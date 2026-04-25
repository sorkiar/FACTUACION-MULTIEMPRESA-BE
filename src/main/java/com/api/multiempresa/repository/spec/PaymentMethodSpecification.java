package com.api.multiempresa.repository.spec;

import com.api.multiempresa.dto.entity.PaymentMethod;
import com.api.multiempresa.dto.filter.PaymentMethodFilter;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class PaymentMethodSpecification {
  public static Specification<PaymentMethod> byFilter(PaymentMethodFilter filter) {
    return (root, query, cb) -> {

      List<Predicate> predicates = new ArrayList<>();

      predicates.add(cb.notEqual(root.get("status"), 2));

      if (filter.getId() != null) {
        predicates.add(cb.equal(root.get("id"), filter.getId()));
      }

      if (filter.getStatus() != null) {
        predicates.add(cb.equal(root.get("status"), filter.getStatus()));
      }

      if (filter.getCode() != null) {
        predicates.add(cb.like(
            cb.lower(root.get("code")),
            "%" + filter.getCode().toLowerCase() + "%"));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
