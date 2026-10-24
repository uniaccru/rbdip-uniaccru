ALTER TABLE customers
    ADD COLUMN first_name VARCHAR(255),
    ADD COLUMN last_name VARCHAR(255),
    ALTER COLUMN full_name DROP NOT NULL;

CREATE FUNCTION sync_customer_name_columns()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        IF NEW.full_name IS NOT NULL THEN
            NEW.first_name := split_part(trim(NEW.full_name), ' ', 1);
            NEW.last_name := CASE
                WHEN position(' ' IN trim(NEW.full_name)) > 0
                    THEN substring(trim(NEW.full_name) FROM position(' ' IN trim(NEW.full_name)) + 1)
                ELSE ''
            END;
        ELSIF NEW.first_name IS NOT NULL AND NEW.last_name IS NOT NULL THEN
            NEW.full_name := concat_ws(' ', NEW.first_name, NEW.last_name);
        END IF;
    ELSIF NEW.full_name IS DISTINCT FROM OLD.full_name
          AND NEW.full_name IS NOT NULL THEN
        NEW.first_name := split_part(trim(NEW.full_name), ' ', 1);
        NEW.last_name := CASE
            WHEN position(' ' IN trim(NEW.full_name)) > 0
                THEN substring(trim(NEW.full_name) FROM position(' ' IN trim(NEW.full_name)) + 1)
            ELSE ''
        END;
    ELSIF (NEW.first_name IS DISTINCT FROM OLD.first_name
           OR NEW.last_name IS DISTINCT FROM OLD.last_name)
          AND NEW.first_name IS NOT NULL
          AND NEW.last_name IS NOT NULL THEN
        NEW.full_name := concat_ws(' ', NEW.first_name, NEW.last_name);
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER customer_name_dual_write
BEFORE INSERT OR UPDATE ON customers
FOR EACH ROW
EXECUTE FUNCTION sync_customer_name_columns();
