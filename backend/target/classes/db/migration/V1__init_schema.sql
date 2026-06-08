-- 1. Business Units (BUs)
CREATE TABLE business_units (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Companies (With hierarchy)
CREATE TABLE companies (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    parent_company_id UUID REFERENCES companies(id),
    business_unit_id UUID NOT NULL REFERENCES business_units(id),
    profile_data JSONB, -- For company-specific profile info
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Contacts (Temporal + Dynamic)
CREATE TABLE contacts (
    id UUID PRIMARY KEY,
    business_unit_id UUID NOT NULL REFERENCES business_units(id),
    company_id UUID REFERENCES companies(id),
    
    -- Common attributes (Fixed schema)
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(255),
    phone_mobile VARCHAR(50),
    
    -- Dynamic attributes (No-code engine)
    custom_attributes JSONB DEFAULT '{}',
    
    -- Temporal columns for AS-OF/AS-AT
    valid_from TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valid_to TIMESTAMP DEFAULT '9999-12-31 23:59:59',
    
    -- Auditing
    created_by VARCHAR(100),
    last_modified_by VARCHAR(100)
);

-- 4. Enable RLS
ALTER TABLE contacts ENABLE ROW LEVEL SECURITY;

CREATE POLICY bu_isolation_policy ON contacts
USING (business_unit_id = current_setting('app.current_bu_id')::uuid);

-- 5. Audit Logging Table
CREATE TABLE audit_logs (
    id SERIAL PRIMARY KEY,
    entity_type VARCHAR(50),
    entity_id UUID,
    action VARCHAR(20), -- INSERT, UPDATE, DELETE
    actor_id VARCHAR(100),
    old_data JSONB,
    new_data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);