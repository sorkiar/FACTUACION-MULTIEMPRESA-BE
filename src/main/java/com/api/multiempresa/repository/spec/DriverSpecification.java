package com.api.multiempresa.repository.spec;

import com.api.multiempresa.dto.entity.Driver;
import com.api.multiempresa.dto.filter.DriverFilter;
import com.api.multiempresa.util.TenantContext;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class DriverSpecification {

  public static Specification<Driver> byFilter(DriverFilter filter) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      Long companyId = TenantContext.getCurrentCompanyId();
      if (companyId != null) {
        predicates.add(cb.equal(root.get("company").get("id"), companyId));
      }
      predicates.add(cb.notEqual(root.get("status"), 2));

      if (filter.getId() != null) {
        predicates.add(cb.equal(root.get("id"), filter.getId()));
      }
      if (filter.getStatus() != null) {
        predicates.add(cb.equal(root.get("status"), filter.getStatus()));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
