UPDATE customers
SET first_name = split_part(trim(full_name), ' ', 1),
    last_name = CASE
        WHEN position(' ' IN trim(full_name)) > 0
            THEN substring(trim(full_name) FROM position(' ' IN trim(full_name)) + 1)
        ELSE ''
    END
WHERE first_name IS NULL
   OR last_name IS NULL;
