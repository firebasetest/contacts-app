CREATE POLICY bu_isolation_policy ON contacts
USING (
    business_unit_id = current_setting('app.current_bu_id')::uuid 
    OR current_setting('app.is_system_admin') = 'true'
);