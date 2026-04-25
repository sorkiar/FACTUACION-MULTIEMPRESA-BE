package com.api.multiempresa.dto.mapper;

import com.api.multiempresa.dto.entity.Client;
import com.api.multiempresa.dto.entity.Document;
import com.api.multiempresa.dto.entity.PaymentMethod;
import com.api.multiempresa.dto.entity.Sale;
import com.api.multiempresa.dto.entity.SaleInstallment;
import com.api.multiempresa.dto.entity.SaleItem;
import com.api.multiempresa.dto.entity.SalePayment;
import com.api.multiempresa.dto.entity.SaleRelatedGuide;
import com.api.multiempresa.dto.response.ClientResponse;
import com.api.multiempresa.dto.response.DocumentResponse;
import com.api.multiempresa.dto.response.PaymentMethodResponse;
import com.api.multiempresa.dto.response.SaleInstallmentResponse;
import com.api.multiempresa.dto.response.SaleItemResponse;
import com.api.multiempresa.dto.response.SalePaymentResponse;
import com.api.multiempresa.dto.response.SaleResponse;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SaleMapper {
  // hasRetention, retentionAmount, retentionRate, netAmount → auto-mapped by name
  // hasDetraction, detractionCode, detractionRate, detractionAmount, detractionAccount → auto-mapped by name
  @Mapping(target = "document", expression = "java(mapLastDocument(entity.getDocuments()))")
  @Mapping(target = "relatedGuides", expression = "java(mapRelatedGuides(entity.getRelatedGuides()))")
  SaleResponse toResponse(Sale entity);

  List<SaleResponse> toResponseList(List<Sale> entities);

  @Mapping(target = "productId", source = "product.id")
  @Mapping(target = "serviceId", source = "service.id")
  SaleItemResponse toItemResponse(SaleItem entity);

  List<SaleItemResponse> toItemResponseList(List<SaleItem> entities);

  DocumentResponse toDocumentResponse(Document entity);

  @Mapping(target = "personTypeId", source = "personType.id")
  @Mapping(target = "personType", source = "personType.name")
  @Mapping(target = "documentTypeId", source = "documentType.id")
  @Mapping(target = "documentType", source = "documentType.name")
  @Mapping(target = "birthDate", source = "birthDate", dateFormat = "yyyy-MM-dd")
  @Mapping(target = "addresses", ignore = true)
  ClientResponse toClientResponse(Client entity);

  PaymentMethodResponse toPaymentMethodResponse(PaymentMethod entity);

  @Mapping(target = "paymentMethod", source = "paymentMethod")
  SalePaymentResponse toPaymentResponse(SalePayment entity);

  List<SalePaymentResponse> toPaymentResponseList(List<SalePayment> entities);

  @Mapping(target = "dueDate", source = "dueDate", dateFormat = "yyyy-MM-dd")
  SaleInstallmentResponse toInstallmentResponse(SaleInstallment entity);

  List<SaleInstallmentResponse> toInstallmentResponseList(List<SaleInstallment> entities);

  default List<String> mapRelatedGuides(Set<SaleRelatedGuide> guides) {
    if (guides == null || guides.isEmpty()) return List.of();
    return guides.stream()
        .map(SaleRelatedGuide::getGuideNumber)
        .collect(Collectors.toList());
  }

  default DocumentResponse mapLastDocument(Set<Document> documents) {
    if (documents == null || documents.isEmpty()) {
      return null;
    }

    return documents.stream()
        .filter(doc -> doc.getDocumentTypeSunat() != null
                && doc.getDocumentTypeSunat().getCode() != null
                && (
                "01".equals(doc.getDocumentTypeSunat().getCode()) ||
                    "03".equals(doc.getDocumentTypeSunat().getCode())
            )
        )
        .max(Comparator.comparing(Document::getCreatedAt))
        .map(this::toDocumentResponse)
        .orElse(null);
  }

}
