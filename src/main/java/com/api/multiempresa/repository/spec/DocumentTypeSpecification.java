package com.api.multiempresa.repository.spec;

import com.api.multiempresa.dto.entity.DocumentType;
import com.api.multiempresa.dto.filter.DocumentTypeFilter;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class DocumentTypeSpecification {
  public static Specification<DocumentType> byFilter(DocumentTypeFilter filter) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      predicates.add(cb.notEqual(root.get("status"), 2));

      if (filter.getStatus() != null) {
        predicates.add(cb.equal(root.get("status"), filter.getStatus()));
      }

      if (filter.getPersonTypeId() != null) {
        predicates.add(cb.equal(root.get("personType").get("id"), filter.getPersonTypeId()));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };

  }
}
