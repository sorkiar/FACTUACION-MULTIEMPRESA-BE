package com.api.multiempresa.repository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SkuSequenceRepository {
  private final JdbcTemplate jdbcTemplate;

  public int getCurrentValue(String type) {
    Integer current = jdbcTemplate.queryForObject(
        "SELECT last_seq_value FROM sku_sequence WHERE type = ?",
        Integer.class,
        type
    );

    return current != null ? current : 0;
  }

  @Transactional
  public int registerSku(String type) {
    Integer current = jdbcTemplate.queryForObject(
        "SELECT last_seq_value FROM sku_sequence WHERE type = ? FOR UPDATE",
        Integer.class,
        type
    );

    int next = current + 1;

    jdbcTemplate.update(
        "UPDATE sku_sequence SET last_seq_value = ? WHERE type = ?",
        next,
        type
    );

    return next;
  }
}
