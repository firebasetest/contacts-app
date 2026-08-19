CREATE MATERIALIZED VIEW contact_summary_view AS
SELECT 
    business_unit_id,
    status,
    COUNT(*) as total_contacts
FROM contacts
WHERE valid_to = '9999-12-31'
GROUP BY business_unit_id, status;

-- Index it for ultra-fast reporting
CREATE INDEX idx_summary_bu ON contact_summary_view(business_unit_id);