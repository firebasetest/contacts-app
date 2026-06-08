-- Create a summarized view that flattens JSONB keys for fast aggregation
CREATE MATERIALIZED VIEW contact_analytics_view AS
SELECT 
    business_unit_id,
    status,
    (custom_attributes->>'region') as region,
    COUNT(*) as contact_count
FROM contacts
WHERE valid_to = '9999-12-31'
GROUP BY 1, 2, 3;

-- Create an index to support fast dashboard filtering by Business Unit
CREATE INDEX idx_analytics_bu ON contact_analytics_view(business_unit_id);