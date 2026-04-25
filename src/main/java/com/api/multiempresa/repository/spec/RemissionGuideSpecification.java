package com.api.multiempresa.repository.spec;

import com.api.multiempresa.dto.entity.RemissionGuide;
import com.api.multiempresa.dto.filter.RemissionGuideFilter;
import com.api.multiempresa.util.TenantContext;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class RemissionGuideSpecification {

  public static Specification<RemissionGuide> byFilter(RemissionGuideFilter filter) {

    return (root, query, cb) -> {

      List<Predicate> predicates = new ArrayList<>();
      Long companyId = TenantContext.getCurrentCompanyId();
      if (companyId != null) {
        predicates.add(cb.equal(root.get("company").get("id"), companyId));
      }

      predicates.add(cb.isNull(root.get("deletedAt")));

      if (filter.getId() != null) {
        predicates.add(cb.equal(root.get("id"), filter.getId()));
      }

      if (filter.getSeries() != null && !filter.getSeries().isBlank()) {
        predicates.add(cb.equal(root.get("series"), filter.getSeries()));
      }

      if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
        predicates.add(cb.equal(root.get("status"), filter.getStatus()));
      }

      if (filter.getTransferReason() != null && !filter.getTransferReason().isBlank()) {
        predicates.add(cb.equal(root.get("transferReason"), filter.getTransferReason()));
      }

      if (filter.getTransportMode() != null && !filter.getTransportMode().isBlank()) {
        predicates.add(cb.equal(root.get("transportMode"), filter.getTransportMode()));
      }

      if (filter.getStartDate() != null && filter.getEndDate() != null) {
        LocalDateTime start = filter.getStartDate().atStartOfDay();
        LocalDateTime end = filter.getEndDate().atTime(23, 59, 59);
        predicates.add(cb.between(root.get("issueDate"), start, end));
      }

      query.orderBy(cb.desc(root.get("issueDate")));
      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
