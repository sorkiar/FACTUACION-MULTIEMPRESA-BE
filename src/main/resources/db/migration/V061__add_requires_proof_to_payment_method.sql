ALTER TABLE payment_method
    ADD COLUMN requires_proof TINYINT(1) NOT NULL DEFAULT 0;
