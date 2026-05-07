-- V046: Backfill person_type_id on document_type rows seeded before V039
--   DNI / CE / PASS → NAT (Persona Natural)
--   RUC              → JUR (Persona Jurídica)
UPDATE document_type dt
    JOIN person_type pt ON pt.code = 'NAT'
SET dt.person_type_id = pt.id
WHERE dt.code IN ('DNI', 'CE', 'PASS')
  AND dt.person_type_id IS NULL;

UPDATE document_type dt
    JOIN person_type pt ON pt.code = 'JUR'
SET dt.person_type_id = pt.id
WHERE dt.code = 'RUC'
  AND dt.person_type_id IS NULL;
