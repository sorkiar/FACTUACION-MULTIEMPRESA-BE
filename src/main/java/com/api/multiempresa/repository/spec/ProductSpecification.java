package com.api.multiempresa.repository.spec;

import com.api.multiempresa.dto.entity.Product;
import com.api.multiempresa.dto.filter.ProductFilter;
import com.api.multiempresa.util.TenantContext;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecification {
  public static Specification<Product> byFilter(ProductFilter filter) {
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

      if (filter.getCategoryId() != null) {
        predicates.add(cb.equal(root.get("category").get("id"), filter.getCategoryId()));
      }

      if (filter.getSku() != null) {
        predicates.add(cb.like(root.get("sku"), "%" + filter.getSku() + "%"));
      }

      if ("PEN".equalsIgnoreCase(filter.getCurrencyCode())) {
        predicates.add(cb.and(
            cb.isNotNull(root.get("salePricePen")),
            cb.greaterThan(root.get("salePricePen"), BigDecimal.ZERO)));
      } else if ("USD".equalsIgnoreCase(filter.getCurrencyCode())) {
        predicates.add(cb.and(
            cb.isNotNull(root.get("salePriceUsd")),
            cb.greaterThan(root.get("salePriceUsd"), BigDecimal.ZERO)));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
