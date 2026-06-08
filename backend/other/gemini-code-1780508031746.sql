CREATE OR REPLACE PROCEDURE batch_upsert_contacts(batch_data JSONB)
LANGUAGE plpgsql
AS $$
BEGIN
    -- 1. Close historical versions of contacts found in the batch
    UPDATE contacts c
    SET valid_to = NOW()
    FROM jsonb_to_recordset(batch_data) AS b(external_id TEXT)
    WHERE c.external_id = b.external_id AND c.valid_to = '9999-12-31';

    -- 2. Insert new versions
    INSERT INTO contacts (id, external_id, first_name, ..., valid_from)
    SELECT gen_random_uuid(), b.external_id, b.first_name, ..., NOW()
    FROM jsonb_to_recordset(batch_data) AS b(...);
END;
$$;