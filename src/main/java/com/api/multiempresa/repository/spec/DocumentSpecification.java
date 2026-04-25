package com.api.multiempresa.repository.spec;

import com.api.multiempresa.dto.entity.Document;
import com.api.multiempresa.dto.filter.DocumentFilter;
import com.api.multiempresa.util.TenantContext;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class DocumentSpecification {
  public static Specification<Document> byFilter(DocumentFilter filter) {

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

      if (filter.getSaleId() != null) {
        predicates.add(cb.equal(
            root.get("sale").get("id"),
            filter.getSaleId()));
      }

      if (filter.getIssueDate() != null) {

        LocalDateTime start = filter.getIssueDate().atStartOfDay();
        LocalDateTime end = filter.getIssueDate().atTime(23, 59, 59);

        predicates.add(cb.between(
            root.get("issueDate"),
            start,
            end));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
