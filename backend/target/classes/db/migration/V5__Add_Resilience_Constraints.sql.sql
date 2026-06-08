-- V5__Add_Resilience_Constraints.sql
ALTER TABLE import_jobs 
ADD CONSTRAINT check_valid_status 
CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED'));

-- Ensure index exists for the recovery query
CREATE INDEX idx_import_jobs_status_updated 
ON import_jobs(status, updated_at);