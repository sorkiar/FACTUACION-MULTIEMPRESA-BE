-- V049: Sync detraction_code status to match reference catalog (SUNAT Catálogo 54).
-- V015 inserted all BIEN codes as inactive (status=0); reference project has them active.

-- BIENES (Anexo 1) — activate all
UPDATE detraction_code
SET status = 1
WHERE code IN ('001','002','003','004','005','006','007','008','009','010',
               '011','012','013','014','015','016','017','018',
               '031','032','033','034','035','036');

-- SERVICIOS — activate codes that are active in reference
UPDATE detraction_code
SET status = 1
WHERE code IN ('024');
