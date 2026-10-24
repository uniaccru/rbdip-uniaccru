DROP TRIGGER customer_name_dual_write ON customers;
DROP FUNCTION sync_customer_name_columns();

ALTER TABLE customers
    ALTER COLUMN first_name SET NOT NULL,
    ALTER COLUMN last_name SET NOT NULL;

ALTER TABLE customers
    DROP COLUMN full_name;
