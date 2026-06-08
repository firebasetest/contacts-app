CREATE TABLE contacts (
    id UUID PRIMARY KEY,
    name TEXT,
    status TEXT,
    business_unit_id UUID,
    custom_attributes JSONB,
    -- Discriminator Column
    contact_type VARCHAR(20), 
    -- Specialized columns (NULL if not applicable)
    tax_id TEXT,             -- For Company
    industry TEXT,           -- For Company
    employee_id TEXT,        -- For Employee
    department TEXT,         -- For Employee
    company_id UUID          -- For Employee
);

-- Index the discriminator for high-performance filtering
CREATE INDEX idx_contacts_type ON contacts(contact_type);