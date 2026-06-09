-- Clean up existing tables and constraints
DROP POLICY IF EXISTS tenant_isolation_policy_audit ON audit_logs;
DROP POLICY IF EXISTS tenant_isolation_policy_jobs ON import_jobs;
DROP POLICY IF EXISTS tenant_isolation_policy ON contacts;

DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS import_jobs;
DROP TABLE IF EXISTS contacts;
DROP TABLE IF EXISTS attribute_definitions;
DROP TABLE IF EXISTS business_units;

-- 1. Business Units Table (Core Tenant Base)
CREATE TABLE business_units (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 2. Attribute Definitions Table (Metadata Field Engine Metadata)
CREATE TABLE attribute_definitions (
    id UUID PRIMARY KEY,
    business_unit_id UUID NOT NULL REFERENCES business_units(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    data_type VARCHAR(50) NOT NULL,
    required BOOLEAN DEFAULT FALSE NOT NULL,
    validation_rules JSONB DEFAULT '{}'::jsonb NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UNIQUE (business_unit_id, name)
);

-- 3. Unified Contacts & Accounts Table (Single-Table Strategy matching JPA Structure)
CREATE TABLE contacts (
    id UUID PRIMARY KEY,
    business_unit_id UUID NOT NULL REFERENCES business_units(id) ON DELETE RESTRICT,
    parent_company_id UUID REFERENCES contacts(id) ON DELETE SET NULL, -- Handles Company Hierarchies
    contact_type VARCHAR(50) NOT NULL,                                 -- GENERAL vs COMPANY discriminator
    name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    custom_attributes JSONB DEFAULT '{}'::jsonb NOT NULL,               -- Dynamic dynamic attributes bucket
    
    -- General Contact Specific Fields
    email VARCHAR(255),
    phone_number VARCHAR(100),
    source VARCHAR(100),
    
    -- Company Specific Fields
    tax_id VARCHAR(100),
    industry VARCHAR(150),
    
    -- Auditing Metadata
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255)
);

-- 4. Background Job Management Table (Resilience & Recovery Model)
CREATE TABLE import_jobs (
    job_id UUID PRIMARY KEY,
    business_unit_id UUID NOT NULL REFERENCES business_units(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL,
    total_records INT DEFAULT 0 NOT NULL,
    processed_records INT DEFAULT 0 NOT NULL,
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    CONSTRAINT check_valid_status CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED'))
);

-- 5. Audit Logging Table (Security Monitoring & Change Tracking)
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    business_unit_id UUID NOT NULL REFERENCES business_units(id) ON DELETE CASCADE,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(20) NOT NULL, -- INSERT, UPDATE, DELETE
    actor_id VARCHAR(100),
    old_data JSONB DEFAULT '{}'::jsonb,
    new_data JSONB DEFAULT '{}'::jsonb,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 6. Indexes for Performance Isolation & Fast Lookups
CREATE INDEX idx_contacts_bu_id ON contacts(business_unit_id);
CREATE INDEX idx_contacts_parent ON contacts(parent_company_id);
CREATE INDEX idx_contacts_status ON contacts(status);
CREATE INDEX idx_import_jobs_bu_id ON import_jobs(business_unit_id);
CREATE INDEX idx_import_jobs_status_updated ON import_jobs(status, updated_at);
CREATE INDEX idx_attr_definitions_bu ON attribute_definitions(business_unit_id);
CREATE INDEX idx_audit_logs_bu_entity ON audit_logs(business_unit_id, entity_type, entity_id);

-- 7. Enforce Row-Level Security (RLS) across Multi-Tenant Layers
ALTER TABLE contacts ENABLE ROW LEVEL SECURITY;
ALTER TABLE import_jobs ENABLE ROW LEVEL SECURITY;
ALTER TABLE audit_logs ENABLE ROW LEVEL SECURITY;

-- Dynamic Tenant Matching Rule (Defaults to safe fallback UUID if context parameter is unset)
CREATE POLICY tenant_isolation_policy ON contacts 
    USING (business_unit_id = COALESCE(NULLIF(current_setting('app.current_tenant', true), ''), '00000000-0000-0000-0000-000000000000')::uuid);

CREATE POLICY tenant_isolation_policy_jobs ON import_jobs 
    USING (business_unit_id = COALESCE(NULLIF(current_setting('app.current_tenant', true), ''), '00000000-0000-0000-0000-000000000000')::uuid);

CREATE POLICY tenant_isolation_policy_audit ON audit_logs 
    USING (business_unit_id = COALESCE(NULLIF(current_setting('app.current_tenant', true), ''), '00000000-0000-0000-0000-000000000000')::uuid);

-- Add Authorization columns to the unified contacts architecture
ALTER TABLE contacts ADD COLUMN system_role VARCHAR(50) DEFAULT 'REGULAR' NOT NULL;
ALTER TABLE contacts ADD COLUMN external_user_id VARCHAR(255); -- Maps to OIDC JWT 'sub' claim

-- Ensure an external user ID can only be mapped once per Business Unit
ALTER TABLE contacts ADD CONSTRAINT unique_bu_external_user UNIQUE (business_unit_id, external_user_id);

-- Constraint safeguarding roles
ALTER TABLE contacts ADD CONSTRAINT check_valid_system_role 
    CHECK (system_role IN ('INTERNAL_EMPLOYEE', 'DELEGATED_ADMIN', 'REGULAR'));

-- Create specialized indexes for authorization lookups
CREATE INDEX idx_contacts_security_lookup ON contacts (external_user_id, system_role);

-- 1. Add operational notes column to the core table
ALTER TABLE contacts ADD COLUMN notes TEXT;

-- 2. Build the System-Versioned Temporal History Table
CREATE TABLE contacts_history (
    history_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contact_id UUID NOT NULL,
    business_unit_id UUID NOT NULL,
    name VARCHAR(255),
    status VARCHAR(50),
    email VARCHAR(255),
    phone_number VARCHAR(50),
    notes TEXT,
    custom_attributes JSONB,
    system_role VARCHAR(50),
    
    -- Temporal Boundaries (AS-OF / AS-AT)
    valid_from TIMESTAMP NOT NULL,
    valid_to TIMESTAMP NOT NULL,
    
    -- Capture of mutating actor
    modified_by VARCHAR(100),
    change_action VARCHAR(20) -- 'UPDATE' or 'DELETE'
);

-- 3. Optimization Indexes for High-Performance Historical Queries
CREATE INDEX idx_contacts_temporal_range 
ON contacts_history (contact_id, valid_from, valid_to);

CREATE INDEX idx_contacts_temporal_bu_lookup 
ON contacts_history (business_unit_id, valid_from, valid_to);