package com.api.multiempresa.repository;

import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SkuSequenceRepository {

  private final JdbcTemplate jdbcTemplate;

  public int getCurrentValue(String type, Long companyId) {
    List<Integer> rows = jdbcTemplate.queryForList(
        "SELECT last_seq_value FROM sku_sequence WHERE company_id = ? AND type = ?",
        Integer.class, companyId, type
    );
    return rows.isEmpty() || rows.get(0) == null ? 0 : rows.get(0);
  }

  @Transactional
  public int registerSku(String type, Long companyId) {
    // Ensure row exists before locking
    jdbcTemplate.update(
        "INSERT IGNORE INTO sku_sequence (company_id, type, last_seq_value) VALUES (?, ?, 0)",
        companyId, type
    );

    Integer current = jdbcTemplate.queryForObject(
        "SELECT last_seq_value FROM sku_sequence WHERE company_id = ? AND type = ? FOR UPDATE",
        Integer.class, companyId, type
    );

    int next = (current != null ? current : 0) + 1;

    jdbcTemplate.update(
        "UPDATE sku_sequence SET last_seq_value = ? WHERE company_id = ? AND type = ?",
        next, companyId, type
    );

    return next;
  }
}
