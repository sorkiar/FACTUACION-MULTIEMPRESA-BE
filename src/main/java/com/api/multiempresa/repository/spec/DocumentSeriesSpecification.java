package com.api.multiempresa.repository.spec;

import com.api.multiempresa.dto.entity.DocumentSeries;
import com.api.multiempresa.dto.filter.DocumentSeriesFilter;
import com.api.multiempresa.util.TenantContext;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class DocumentSeriesSpecification {
  public static Specification<DocumentSeries> byFilter(DocumentSeriesFilter filter) {

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

      if (filter.getDocumentTypeCode() != null) {
        predicates.add(cb.equal(
            root.get("documentTypeSunat").get("code"),
            filter.getDocumentTypeCode()));
      }

      if (filter.getSeries() != null) {
        predicates.add(cb.like(
            cb.lower(root.get("series")),
            "%" + filter.getSeries().toLowerCase() + "%"));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
