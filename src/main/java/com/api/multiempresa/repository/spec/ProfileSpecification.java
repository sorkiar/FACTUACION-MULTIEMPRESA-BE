package com.api.multiempresa.repository.spec;

import com.api.multiempresa.dto.entity.Profile;
import com.api.multiempresa.dto.filter.ProfileFilter;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class ProfileSpecification {

  public static Specification<Profile> byFilter(ProfileFilter filter) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      // never list deleted or is_system profiles
      predicates.add(cb.notEqual(root.get("status"), 2));
      predicates.add(cb.isFalse(root.get("isSystem")));

      // restrict to current tenant company
      if (filter.getCompanyId() != null) {
        predicates.add(cb.equal(root.get("company").get("id"), filter.getCompanyId()));
      }

      if (filter.getName() != null) {
        predicates.add(cb.like(
            cb.lower(root.get("name")),
            "%" + filter.getName().toLowerCase() + "%"));
      }
      if (filter.getStatus() != null) {
        predicates.add(cb.equal(root.get("status"), filter.getStatus()));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
