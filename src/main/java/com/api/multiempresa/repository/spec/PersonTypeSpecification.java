package com.api.multiempresa.repository.spec;

import com.api.multiempresa.dto.entity.PersonType;
import com.api.multiempresa.dto.filter.PersonTypeFilter;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class PersonTypeSpecification {
  public static Specification<PersonType> byFilter(PersonTypeFilter filter) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      predicates.add(cb.notEqual(root.get("status"), 2));

      if (filter.getStatus() != null) {
        predicates.add(cb.equal(root.get("status"), filter.getStatus()));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };

  }
}
