package com.api.multiempresa.repository.spec;

import com.api.multiempresa.dto.entity.Ubigeo;
import com.api.multiempresa.dto.filter.UbigeoFilter;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class UbigeoSpecification {

  private UbigeoSpecification() {}

  public static Specification<Ubigeo> byFilter(UbigeoFilter filter) {
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
