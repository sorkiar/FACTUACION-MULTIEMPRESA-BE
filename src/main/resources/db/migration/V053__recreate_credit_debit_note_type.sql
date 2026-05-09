SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `credit_debit_note_type`;

CREATE TABLE `credit_debit_note_type` (
  `code`          VARCHAR(4)   NOT NULL,
  `name`          VARCHAR(255) NOT NULL,
  `note_category` VARCHAR(10)  NOT NULL COMMENT 'CREDITO o DEBITO',
  `status`        TINYINT      NOT NULL DEFAULT 1,
  `created_at`    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by`    VARCHAR(50)  NOT NULL,
  `updated_at`    TIMESTAMP    NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `updated_by`    VARCHAR(50)  NULL DEFAULT NULL,
  `deleted_at`    TIMESTAMP    NULL DEFAULT NULL,
  `deleted_by`    VARCHAR(50)  NULL DEFAULT NULL,
  PRIMARY KEY (`code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

INSERT INTO `credit_debit_note_type` VALUES ('C01', 'Anulación de la operación',                'CREDITO', 1, '2026-02-17 20:52:49', 'system', NULL,                  NULL,     NULL, NULL);
INSERT INTO `credit_debit_note_type` VALUES ('C02', 'Anulación por error en el RUC',            'CREDITO', 0, '2026-02-17 20:52:49', 'system', '2026-02-28 00:54:42', 'system', NULL, NULL);
INSERT INTO `credit_debit_note_type` VALUES ('C03', 'Corrección por error en la descripción',  'CREDITO', 0, '2026-02-17 20:52:49', 'system', '2026-02-28 00:54:42', 'system', NULL, NULL);
INSERT INTO `credit_debit_note_type` VALUES ('C04', 'Descuento global',                         'CREDITO', 0, '2026-02-17 20:52:49', 'system', '2026-02-28 00:54:42', 'system', NULL, NULL);
INSERT INTO `credit_debit_note_type` VALUES ('C05', 'Descuento por ítem',                       'CREDITO', 0, '2026-02-17 20:52:49', 'system', '2026-02-28 00:54:42', 'system', NULL, NULL);
INSERT INTO `credit_debit_note_type` VALUES ('C06', 'Devolución total',                         'CREDITO', 0, '2026-02-17 20:52:49', 'system', '2026-02-28 00:54:43', 'system', NULL, NULL);
INSERT INTO `credit_debit_note_type` VALUES ('C07', 'Devolución por ítem',                      'CREDITO', 0, '2026-02-17 20:52:49', 'system', '2026-02-28 00:54:43', 'system', NULL, NULL);
INSERT INTO `credit_debit_note_type` VALUES ('C08', 'Bonificación',                             'CREDITO', 0, '2026-02-17 20:52:49', 'system', '2026-02-28 00:54:43', 'system', NULL, NULL);
INSERT INTO `credit_debit_note_type` VALUES ('C09', 'Disminución en el valor',                  'CREDITO', 0, '2026-02-17 20:52:49', 'system', '2026-02-28 00:54:43', 'system', NULL, NULL);
INSERT INTO `credit_debit_note_type` VALUES ('C10', 'Otros conceptos',                          'CREDITO', 0, '2026-02-17 20:52:49', 'system', '2026-02-28 00:54:44', 'system', NULL, NULL);
INSERT INTO `credit_debit_note_type` VALUES ('C11', 'Ajustes de operaciones de exportación',   'CREDITO', 0, '2026-02-17 20:52:49', 'system', '2026-02-28 00:54:44', 'system', NULL, NULL);
INSERT INTO `credit_debit_note_type` VALUES ('C12', 'Ajustes afectos al IVAP',                  'CREDITO', 0, '2026-02-17 20:52:49', 'system', '2026-02-28 00:54:44', 'system', NULL, NULL);
INSERT INTO `credit_debit_note_type` VALUES ('C13', 'Ajustes – montos y/o fechas de pago',     'CREDITO', 0, '2026-02-17 20:52:49', 'system', '2026-02-28 00:54:44', 'system', NULL, NULL);
INSERT INTO `credit_debit_note_type` VALUES ('D01', 'Intereses por mora',                       'DEBITO',  0, '2026-02-17 20:52:49', 'system', '2026-02-28 00:54:45', 'system', NULL, NULL);
INSERT INTO `credit_debit_note_type` VALUES ('D02', 'Aumento en el valor',                      'DEBITO',  1, '2026-02-17 20:52:49', 'system', NULL,                  NULL,     NULL, NULL);
INSERT INTO `credit_debit_note_type` VALUES ('D03', 'Penalidades / otros conceptos',            'DEBITO',  0, '2026-02-17 20:52:49', 'system', '2026-02-28 00:54:46', 'system', NULL, NULL);
INSERT INTO `credit_debit_note_type` VALUES ('D11', 'Ajustes de operaciones de exportación',   'DEBITO',  0, '2026-02-17 20:52:49', 'system', '2026-02-28 00:54:46', 'system', NULL, NULL);
INSERT INTO `credit_debit_note_type` VALUES ('D12', 'Ajustes afectos al IVAP',                  'DEBITO',  0, '2026-02-17 20:52:49', 'system', '2026-02-28 00:54:47', 'system', NULL, NULL);

SET FOREIGN_KEY_CHECKS = 1;
