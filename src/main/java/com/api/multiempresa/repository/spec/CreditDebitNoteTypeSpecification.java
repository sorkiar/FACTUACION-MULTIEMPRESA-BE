package com.api.multiempresa.repository.spec;

import com.api.multiempresa.dto.entity.CreditDebitNoteType;
import com.api.multiempresa.dto.filter.CreditDebitNoteTypeFilter;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class CreditDebitNoteTypeSpecification {

  private CreditDebitNoteTypeSpecification() {}

  public static Specification<CreditDebitNoteType> byFilter(CreditDebitNoteTypeFilter filter) {
    return (root, query, cb) -> {

      List<Predicate> predicates = new ArrayList<>();

      if (filter.getCode() != null && !filter.getCode().isBlank()) {
        predicates.add(cb.equal(
            cb.upper(root.get("code")),
            filter.getCode().toUpperCase()));
      }

      if (filter.getNoteCategory() != null && !filter.getNoteCategory().isBlank()) {
        predicates.add(cb.equal(
            cb.upper(root.get("noteCategory")),
            filter.getNoteCategory().toUpperCase()));
      }

      if (filter.getStatus() != null) {
        predicates.add(cb.equal(root.get("status"), filter.getStatus()));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
