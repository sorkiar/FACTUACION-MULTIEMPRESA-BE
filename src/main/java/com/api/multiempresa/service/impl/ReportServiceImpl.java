package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.entity.Document;
import com.api.multiempresa.dto.entity.Sale;
import com.api.multiempresa.dto.entity.SaleItem;
import com.api.multiempresa.dto.response.SalesReportResponse;
import com.api.multiempresa.dto.response.SalesReportRowResponse;
import com.api.multiempresa.repository.SaleRepository;
import com.api.multiempresa.service.ConfigurationService;
import com.api.multiempresa.service.ReportService;
import com.api.multiempresa.util.TenantContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

  private final SaleRepository saleRepository;
  private final ConfigurationService configurationService;

  @Override
  public SalesReportResponse salesReport(
      LocalDate startDate,
      LocalDate endDate,
      String clientIds,
      String productIds
  ) {
    LocalDateTime start = startDate.atStartOfDay();
    LocalDateTime end = endDate.atTime(23, 59, 59);
    Long companyId = TenantContext.getCurrentCompanyId();

    List<Sale> sales = saleRepository.findForReport(start, end, companyId);

    Set<Long> clientIdSet = parseIds(clientIds);
    if (!clientIdSet.isEmpty()) {
      sales = sales.stream()
          .filter(s -> clientIdSet.contains(s.getClient().getId()))
          .collect(Collectors.toList());
    }

    Set<Long> productIdSet = parseIds(productIds);
    if (!productIdSet.isEmpty()) {
      sales = sales.stream()
          .filter(s -> s.getItems().stream()
              .anyMatch(item -> item.getProduct() != null &&
                  productIdSet.contains(item.getProduct().getId())))
          .collect(Collectors.toList());
    }

    Map<String, String> config = configurationService.getGroup("empresa_emisora");
    DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    List<SalesReportRowResponse> rows = new ArrayList<>();

    for (Sale sale : sales) {

      Document doc = sale.getDocuments().stream()
          .min(Comparator.comparing(Document::getIssueDate))
          .orElse(null);

      String document = "-";
      if (doc != null) {
        document = doc.getSeries() + "-" + doc.getSequence();
      }

      String issueDate = sale.getSaleDate().format(dateFmt);
      String client = buildClientField(sale);
      BigDecimal saleTotal = sale.getTotalAmount() != null ? sale.getTotalAmount() : BigDecimal.ZERO;

      for (SaleItem item : sale.getItems()) {
        rows.add(SalesReportRowResponse.builder()
            .issueDate(issueDate)
            .document(document)
            .client(client)
            .itemDescription(buildItemDescription(item))
            .quantity(item.getQuantity())
            .unitPrice(item.getUnitPrice())
            .discountPercentage(item.getDiscountPercentage() != null
                ? item.getDiscountPercentage() : BigDecimal.ZERO)
            .subtotal(item.getTotalAmount() != null ? item.getTotalAmount() : BigDecimal.ZERO)
            .saleTotal(saleTotal)
            .currencyCode(sale.getCurrencyCode())
            .build());
      }
    }

    return SalesReportResponse.builder()
        .companyName(config.get("emprRazonSocial"))
        .dateRange(startDate.format(dateFmt) + " - " + endDate.format(dateFmt))
        .totalItems(rows.size())
        .rows(rows)
        .build();
  }

  private String buildClientField(Sale sale) {
    var client = sale.getClient();
    String name;
    if (client.getBusinessName() != null && !client.getBusinessName().isBlank()) {
      name = client.getBusinessName();
    } else {
      String full = ((client.getFirstName() != null ? client.getFirstName() : "") + " " +
          (client.getLastName() != null ? client.getLastName() : "")).trim();
      name = full.isBlank() ? "" : full;
    }
    return client.getDocumentNumber() + " - " + name;
  }

  private String buildItemDescription(SaleItem item) {
    String sku;
    if (item.getProduct() != null) {
      sku = item.getProduct().getSku();
    } else if (item.getService() != null) {
      sku = item.getService().getSku();
    } else {
      sku = "SRV0000000";
    }
    return sku + " - " + item.getDescription();
  }

  private Set<Long> parseIds(String csv) {
    if (csv == null || csv.isBlank()) return Set.of();
    return Arrays.stream(csv.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .map(Long::parseLong)
        .collect(Collectors.toSet());
  }
}
