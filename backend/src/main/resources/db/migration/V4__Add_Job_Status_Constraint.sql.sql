-- V4__Add_Job_Status_Constraint.sql
ALTER TABLE import_jobs 
ADD CONSTRAINT check_valid_status 
CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED'));