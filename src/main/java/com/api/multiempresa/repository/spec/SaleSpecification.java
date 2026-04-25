package com.api.multiempresa.repository.spec;

import com.api.multiempresa.dto.entity.CreditDebitNote;
import com.api.multiempresa.dto.entity.Sale;
import com.api.multiempresa.dto.filter.SaleFilter;
import com.api.multiempresa.util.TenantContext;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class SaleSpecification {
  public static Specification<Sale> byFilter(SaleFilter filter) {

    return (root, query, cb) -> {

      // Evita filas duplicadas cuando el EntityGraph o joins agreguen rows extra
      if (query != null) query.distinct(true);

      List<Predicate> predicates = new ArrayList<>();
      Long companyId = TenantContext.getCurrentCompanyId();
      if (companyId != null) {
        predicates.add(cb.equal(root.get("company").get("id"), companyId));
      }

      predicates.add(cb.isNull(root.get("deletedAt")));

      if (filter.getId() != null) {
        predicates.add(cb.equal(root.get("id"), filter.getId()));
      }

      if (filter.getClientId() != null) {
        predicates.add(cb.equal(
            root.get("client").get("id"),
            filter.getClientId()));
      }

      if (filter.getSaleStatus() != null) {
        predicates.add(cb.equal(
            root.get("saleStatus"),
            filter.getSaleStatus()));
      }

      if (filter.getPaymentStatus() != null) {
        predicates.add(cb.equal(
            root.get("paymentStatus"),
            filter.getPaymentStatus()));
      }

      if (filter.getStartDate() != null && filter.getEndDate() != null) {
        LocalDateTime start = filter.getStartDate().atStartOfDay();
        LocalDateTime end = filter.getEndDate().atTime(23, 59, 59);

        predicates.add(cb.between(
            root.get("saleDate"),
            start,
            end));
      }

      if (Boolean.TRUE.equals(filter.getExcludeAnnulled())) {
        Subquery<Long> annulledSales = query.subquery(Long.class);
        Root<CreditDebitNote> noteRoot = annulledSales.from(CreditDebitNote.class);
        annulledSales.select(noteRoot.get("sale").get("id"))
            .where(
                cb.equal(noteRoot.get("creditDebitNoteType").get("code"), "C01"),
                noteRoot.get("status").in("PENDIENTE", "ACEPTADO"),
                cb.isNull(noteRoot.get("deletedAt"))
            );
        predicates.add(cb.not(root.get("id").in(annulledSales)));
      }

      query.orderBy(cb.desc(root.get("saleDate")));
      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
