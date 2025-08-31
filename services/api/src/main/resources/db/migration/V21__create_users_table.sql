-- V21__create_users_table.sql
-- Create users table for JWT authentication

CREATE TABLE IF NOT EXISTS "users" (
    "id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
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

-- Insert demo regular user (password: user123 -> BCrypt hash)
INSERT INTO users (username, email, password, role) 
VALUES ('user', 'user@pkudiet.app', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM7lbEOoYGvC4HW.5CwS', 'USER')
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