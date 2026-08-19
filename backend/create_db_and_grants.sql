-- create_db_and_grants.sql
DO $$
BEGIN
IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'contacts_dev') THEN
CREATE DATABASE contacts_dev;
END IF;
END
$$;

DO $$
BEGIN
IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'contacts_dev_user') THEN
CREATE ROLE contacts_dev_user LOGIN PASSWORD 'contacts_dev_password';
ELSE
ALTER ROLE contacts_dev_user WITH PASSWORD 'contacts_dev_password';
END IF;
END
$$;

-- Grant DB-level connect
GRANT CONNECT ON DATABASE contacts_dev TO contacts_dev_user;

-- The rest runs against the contacts_dev DB
\connect contacts_dev

-- Allow using and creating objects in the public schema
GRANT USAGE ON SCHEMA public TO contacts_dev_user;
GRANT CREATE ON SCHEMA public TO contacts_dev_user;

-- Privileges on any existing tables/sequences
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO contacts_dev_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO contacts_dev_user;

-- Default privileges for future objects created by the DB owner (adjust role if needed)
ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO contacts_dev_user;
ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public
GRANT USAGE, SELECT ON SEQUENCES TO contacts_dev_user;

