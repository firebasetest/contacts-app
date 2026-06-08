-- Enable RLS on the table
ALTER TABLE contacts ENABLE ROW LEVEL SECURITY;

-- Create policy: users can only see rows where business_unit_id matches their session variable
CREATE POLICY tenant_isolation_policy ON contacts
    USING (business_unit_id = current_setting('app.current_tenant')::uuid);

-- Repeat for other tables
ALTER TABLE import_jobs ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_policy_jobs ON import_jobs
    USING (business_unit_id = current_setting('app.current_tenant')::uuid);