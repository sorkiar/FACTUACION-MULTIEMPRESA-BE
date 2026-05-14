ALTER TABLE sale_item
    MODIFY COLUMN item_type VARCHAR(20) NOT NULL;

ALTER TABLE credit_debit_note_item
    MODIFY COLUMN item_type VARCHAR(20) NOT NULL;
