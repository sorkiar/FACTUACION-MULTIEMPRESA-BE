ALTER TABLE sale_payment
    RENAME COLUMN amount TO amount_paid,
    RENAME COLUMN reference TO payment_reference,
    RENAME COLUMN notes TO observations,
    ADD COLUMN change_amount DECIMAL(14, 2) NULL AFTER amount_paid,
    ADD COLUMN proof_file_url TEXT NULL AFTER payment_reference;
