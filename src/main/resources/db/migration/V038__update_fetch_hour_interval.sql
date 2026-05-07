-- V038: fetch_hour ahora representa el intervalo en horas entre intentos de obtención del TC
-- Ventanas: 1h, 6h, 11h, 16h, 21h (inicio fijo a la 1 AM Lima)
UPDATE configuration
SET config_value = '5',
    description  = 'Intervalo en horas para reintentar la obtención del tipo de cambio SUNAT (inicio: 1 AM Lima). Ej: 5 → intenta a 1h, 6h, 11h, 16h, 21h'
WHERE config_group = 'tipo_cambio'
  AND config_key = 'fetch_hour';
