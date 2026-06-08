CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE contacts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_unit_id UUID NOT NULL,
    contact_type VARCHAR(20) NOT NULL,
    name VARCHAR(255),
    status VARCHAR(50),
    email VARCHAR(255),
    custom_attributes JSONB,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE import_jobs (
    job_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_unit_id UUID NOT NULL,
    status VARCHAR(50),
    total_records INT,
    processed_records INT,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Indexing for multi-tenancy performance
CREATE INDEX idx_contacts_bu_id ON contacts(business_unit_id);
CREATE INDEX idx_import_jobs_bu_id ON import_jobs(business_unit_id);