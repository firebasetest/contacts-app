CREATE OR REPLACE FUNCTION process_audit_log()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'DELETE') THEN
        INSERT INTO audit_logs(entity_type, entity_id, action, actor_id, old_data)
        VALUES (TG_TABLE_NAME, OLD.id, 'DELETE', current_setting('app.user_id', true), to_jsonb(OLD));
        RETURN OLD;
    ELSIF (TG_OP = 'UPDATE') THEN
        INSERT INTO audit_logs(entity_type, entity_id, action, actor_id, old_data, new_data)
        VALUES (TG_TABLE_NAME, NEW.id, 'UPDATE', current_setting('app.user_id', true), to_jsonb(OLD), to_jsonb(NEW));
        RETURN NEW;
    ELSIF (TG_OP = 'INSERT') THEN
        INSERT INTO audit_logs(entity_type, entity_id, action, actor_id, new_data)
        VALUES (TG_TABLE_NAME, NEW.id, 'INSERT', current_setting('app.user_id', true), to_jsonb(NEW));
        RETURN NEW;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;