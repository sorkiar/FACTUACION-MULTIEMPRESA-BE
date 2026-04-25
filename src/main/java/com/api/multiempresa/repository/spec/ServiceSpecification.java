package com.api.multiempresa.repository.spec;

import com.api.multiempresa.dto.entity.Service;
import com.api.multiempresa.dto.filter.ServiceFilter;
import com.api.multiempresa.util.TenantContext;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class ServiceSpecification {
  public static Specification<Service> byFilter(ServiceFilter filter) {
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

      if (filter.getServiceCategoryId() != null) {
        predicates.add(cb.equal(
            root.get("serviceCategory").get("id"),
            filter.getServiceCategoryId()));
      }

      if (filter.getSku() != null) {
        predicates.add(cb.like(
            cb.lower(root.get("sku")),
            "%" + filter.getSku().toLowerCase() + "%"));
      }

      if (filter.getName() != null) {
        predicates.add(cb.like(
            cb.lower(root.get("name")),
            "%" + filter.getName().toLowerCase() + "%"));
      }

      if ("PEN".equalsIgnoreCase(filter.getCurrencyCode())) {
        predicates.add(cb.greaterThan(root.get("pricePen"), BigDecimal.ZERO));
      } else if ("USD".equalsIgnoreCase(filter.getCurrencyCode())) {
        predicates.add(cb.greaterThan(root.get("priceUsd"), BigDecimal.ZERO));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
