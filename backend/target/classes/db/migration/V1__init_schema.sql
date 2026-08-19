-- ==============================================================
-- V1: UNIFIED SCHEMA SETUP (Single Source of Truth, RLS Compliant)
-- ==============================================================

SET timezone = 'UTC';

-- --------------------------------------------------------------
-- 1. CLEANUP (Idempotency)
-- --------------------------------------------------------------
DROP TABLE IF EXISTS user_consent_record CASCADE;
DROP TABLE IF EXISTS contacts_history CASCADE;
DROP TABLE IF EXISTS audit_logs CASCADE;
DROP TABLE IF EXISTS import_error_logs CASCADE;
DROP TABLE IF EXISTS import_jobs CASCADE;
DROP TABLE IF EXISTS contacts CASCADE;
DROP TABLE IF EXISTS attribute_definitions CASCADE;
DROP TABLE IF EXISTS tenant_settings CASCADE;
DROP TABLE IF EXISTS business_units CASCADE;

-- --------------------------------------------------------------
-- 2. CORE TENANT & SETTINGS TABLES
-- --------------------------------------------------------------
CREATE TABLE business_units (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    active BOOLEAN DEFAULT TRUE NOT NULL
);

CREATE TABLE tenant_settings (
    business_unit_id UUID PRIMARY KEY REFERENCES business_units(id) ON DELETE CASCADE,
    telephony_provider VARCHAR(50) DEFAULT 'NATIVE_TEL' NOT NULL,
    telephony_credentials JSONB DEFAULT '{}'::jsonb,
    is_gdpr_enabled BOOLEAN DEFAULT TRUE NOT NULL,
    is_audit_view_enabled BOOLEAN DEFAULT TRUE NOT NULL
);

CREATE TABLE attribute_definitions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_unit_id UUID NOT NULL REFERENCES business_units(id) ON DELETE CASCADE,
    name VARCHAR(250) NOT NULL,
    data_type VARCHAR(50) NOT NULL,
    required BOOLEAN DEFAULT FALSE NOT NULL,
    validation_rules JSONB DEFAULT '{}'::jsonb NOT NULL,
    CONSTRAINT uq_attribute_definition UNIQUE (business_unit_id, name)
);

-- --------------------------------------------------------------
-- 3. POLYMORPHIC CONTACTS (Single-Table Strategy)
-- --------------------------------------------------------------
CREATE TABLE contacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contact_type VARCHAR(50) NOT NULL, -- Discriminator: GENERAL, COMPANY
    business_unit_id UUID NOT NULL REFERENCES business_units(id) ON DELETE CASCADE,
    parent_company_id UUID REFERENCES contacts(id) ON DELETE SET NULL,
    assigned_admin_id UUID REFERENCES contacts(id) ON DELETE SET NULL,

    -- BaseContact Fields
    name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    system_role VARCHAR(50) DEFAULT 'REGULAR' NOT NULL,
    external_user_id VARCHAR(255),
    notes TEXT,
    custom_attributes JSONB DEFAULT '{}'::jsonb,

    -- Contact (GENERAL) Specific Fields
    email VARCHAR(255),
    phone_number VARCHAR(100),
    source VARCHAR(100),

    -- Company Specific Fields
    tax_id VARCHAR(100),
    industry VARCHAR(150),

    -- Auditing Metadata (BaseEntity)
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),

    CONSTRAINT check_valid_system_role CHECK (system_role IN ('INTERNAL_EMPLOYEE', 'DELEGATED_ADMIN', 'REGULAR')),
    CONSTRAINT unique_bu_external_user UNIQUE (business_unit_id, external_user_id)
);

-- --------------------------------------------------------------
-- 4. HISTORY & AUDITING
-- --------------------------------------------------------------
CREATE TABLE contacts_history (
    history_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contact_id UUID NOT NULL REFERENCES contacts(id) ON DELETE CASCADE,
    business_unit_id UUID NOT NULL REFERENCES business_units(id) ON DELETE CASCADE,
    version INTEGER NOT NULL,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    email VARCHAR(255),
    phone_number VARCHAR(100),
    source VARCHAR(100),
    tax_id VARCHAR(100),
    industry VARCHAR(150),
    notes TEXT,
    custom_attributes JSONB,
    system_role VARCHAR(50) NOT NULL,
    valid_from TIMESTAMP WITH TIME ZONE NOT NULL,
    valid_to TIMESTAMP WITH TIME ZONE NOT NULL,
    modified_by VARCHAR(255) NOT NULL,
    change_action VARCHAR(50) NOT NULL,
    capture_type VARCHAR(50) NOT NULL,
    field_deltas JSONB
);

CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    business_unit_id UUID NOT NULL REFERENCES business_units(id) ON DELETE CASCADE,
    entity_type VARCHAR(50),
    entity_id UUID,
    action VARCHAR(20),
    actor_id VARCHAR(100),
    old_data JSONB DEFAULT '{}'::jsonb,
    new_data JSONB DEFAULT '{}'::jsonb,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- --------------------------------------------------------------
-- 5. BACKGROUND JOBS & IMPORT MANAGEMENT
-- --------------------------------------------------------------
CREATE TABLE import_jobs (
    job_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_unit_id UUID NOT NULL REFERENCES business_units(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL,
    total_records INTEGER DEFAULT 0 NOT NULL,
    processed_records INTEGER DEFAULT 0 NOT NULL,
    inserted_records INTEGER DEFAULT 0 NOT NULL,
    updated_records INTEGER DEFAULT 0 NOT NULL,
    failed_records INTEGER DEFAULT 0 NOT NULL,
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    CONSTRAINT check_valid_status CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED'))
);

CREATE TABLE import_error_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id UUID NOT NULL REFERENCES import_jobs(job_id) ON DELETE CASCADE,
    row_number INTEGER NOT NULL,
    record_identifier VARCHAR(255),
    error_message VARCHAR(1024),
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- --------------------------------------------------------------
-- 6. CONSENT MANAGEMENT
-- --------------------------------------------------------------
CREATE TABLE user_consent_record (
    id BIGSERIAL PRIMARY KEY,
    principal_id UUID NOT NULL,
    consent_purpose VARCHAR(50) NOT NULL,
    granted BOOLEAN NOT NULL DEFAULT FALSE,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_principal_purpose UNIQUE (principal_id, consent_purpose)
);

-- --------------------------------------------------------------
-- 7. PERFORMANCE INDEXES
-- --------------------------------------------------------------
CREATE INDEX idx_contacts_bu_id ON contacts(business_unit_id);
CREATE INDEX idx_contacts_parent ON contacts(parent_company_id);
CREATE INDEX idx_contacts_security_lookup ON contacts (external_user_id, system_role);
CREATE INDEX idx_import_jobs_bu_id ON import_jobs(business_unit_id);
CREATE INDEX idx_import_jobs_status_updated ON import_jobs(status, updated_at);
CREATE INDEX idx_attr_definitions_bu ON attribute_definitions(business_unit_id);
CREATE INDEX idx_audit_logs_bu_entity ON audit_logs(business_unit_id, entity_type, entity_id);
CREATE INDEX idx_contacts_temporal_range ON contacts_history (contact_id, valid_from, valid_to);
CREATE INDEX idx_user_consent_principal_id ON user_consent_record (principal_id);
CREATE INDEX idx_user_consent_purpose ON user_consent_record (consent_purpose);

-- --------------------------------------------------------------
-- 8. ROW LEVEL SECURITY (RLS) POLICIES
-- --------------------------------------------------------------
ALTER TABLE contacts ENABLE ROW LEVEL SECURITY;
ALTER TABLE contacts_history ENABLE ROW LEVEL SECURITY;
ALTER TABLE import_jobs ENABLE ROW LEVEL SECURITY;
ALTER TABLE audit_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE attribute_definitions ENABLE ROW LEVEL SECURITY;
ALTER TABLE tenant_settings ENABLE ROW LEVEL SECURITY;

-- Dynamic Tenant Matching Rules (Defaults to fallback UUID if context parameter is unset)
CREATE POLICY tenant_isolation_policy ON contacts
    USING (business_unit_id = COALESCE(NULLIF(current_setting('app.current_tenant', true), ''), '00000000-0000-0000-0000-000000000000')::uuid);

CREATE POLICY tenant_isolation_policy_history ON contacts_history
    USING (business_unit_id = COALESCE(NULLIF(current_setting('app.current_tenant', true), ''), '00000000-0000-0000-0000-000000000000')::uuid);

CREATE POLICY tenant_isolation_policy_jobs ON import_jobs
    USING (business_unit_id = COALESCE(NULLIF(current_setting('app.current_tenant', true), ''), '00000000-0000-0000-0000-000000000000')::uuid);

CREATE POLICY tenant_isolation_policy_audit ON audit_logs
    USING (business_unit_id = COALESCE(NULLIF(current_setting('app.current_tenant', true), ''), '00000000-0000-0000-0000-000000000000')::uuid);

CREATE POLICY tenant_isolation_policy_attributes ON attribute_definitions
    USING (business_unit_id = COALESCE(NULLIF(current_setting('app.current_tenant', true), ''), '00000000-0000-0000-0000-000000000000')::uuid);

CREATE POLICY tenant_isolation_policy_settings ON tenant_settings
    USING (business_unit_id = COALESCE(NULLIF(current_setting('app.current_tenant', true), ''), '00000000-0000-0000-0000-000000000000')::uuid);

COMMENT ON TABLE user_consent_record IS 'Records the explicit consent granted by a principal (user/tenant) for specific purposes.';
