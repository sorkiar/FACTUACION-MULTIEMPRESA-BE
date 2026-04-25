package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.SunatRequestLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SunatRequestLogRepository extends JpaRepository<SunatRequestLog, Long> {

  List<SunatRequestLog> findByDocumentTypeAndSeriesAndSequenceOrderBySentAtDesc(
      String documentType, String series, String sequence);
}
