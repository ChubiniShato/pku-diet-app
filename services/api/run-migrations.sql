-- Run all migrations manually
-- V1__init.sql
create extension if not exists "uuid-ossp";
create table if not exists food_products (
  id uuid primary key default uuid_generate_v4(),
  name text not null,
  unit text not null, -- g/ml
  protein_100g decimal(8,3) not null,
  phe_100g decimal(8,3) not null,
  kcal_100g int not null,
  category text,
  is_active boolean not null default true
);
create index if not exists idx_food_products_name on food_products (name);

-- V19__create_users_table.sql
CREATE TABLE IF NOT EXISTS "users" (
    "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "username" VARCHAR(50) UNIQUE NOT NULL,
    "email" VARCHAR(255) UNIQUE NOT NULL,
    "password" VARCHAR(255) NOT NULL,
    "role" VARCHAR(50) NOT NULL DEFAULT 'USER',
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "last_login" TIMESTAMP,
    "is_enabled" BOOLEAN NOT NULL DEFAULT TRUE,
    "is_account_non_expired" BOOLEAN NOT NULL DEFAULT TRUE,
    "is_account_non_locked" BOOLEAN NOT NULL DEFAULT TRUE,
    "is_credentials_non_expired" BOOLEAN NOT NULL DEFAULT TRUE
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_enabled ON users(is_enabled);

-- Add constraints
ALTER TABLE users ADD CONSTRAINT chk_users_role 
    CHECK (role IN ('USER', 'ADMIN', 'PATIENT', 'HEALTHCARE_PROVIDER'));

-- Insert default admin user (password: admin123 -> BCrypt hash)
INSERT INTO users (username, email, password, role) 
VALUES ('admin', 'admin@pkudiet.app', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'ADMIN')
ON CONFLICT (username) DO NOTHING;

-- Create trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
