-- V1__Initial_schema.sql
-- Initial database schema for KSeF Hub

-- Tenants table
CREATE TABLE tenants (
    id BIGSERIAL PRIMARY KEY,
    nip VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    full_name VARCHAR(500),
    email VARCHAR(200),
    phone VARCHAR(20),
    address VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT true,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tenant_nip ON tenants(nip);
CREATE INDEX idx_tenant_active ON tenants(active);

-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    active BOOLEAN NOT NULL DEFAULT true,
    email_verified BOOLEAN NOT NULL DEFAULT false,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_tenant ON users(tenant_id);

-- Certificates table
CREATE TABLE certificates (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    certificate_id VARCHAR(100) NOT NULL UNIQUE,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    subject_dn VARCHAR(200) NOT NULL,
    issuer_dn VARCHAR(200) NOT NULL,
    serial_number VARCHAR(100),
    certificate_data TEXT,
    private_key_data TEXT,
    issued_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    fingerprint VARCHAR(500),
    notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_cert_tenant ON certificates(tenant_id);
CREATE INDEX idx_cert_status ON certificates(status);
CREATE INDEX idx_cert_expiry ON certificates(expires_at);

-- KSeF Sessions table
CREATE TABLE ksef_sessions (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    reference_number VARCHAR(100) NOT NULL UNIQUE,
    session_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPENED',
    access_token VARCHAR(500),
    refresh_token VARCHAR(500),
    token_expires_at TIMESTAMP,
    context_identifier VARCHAR(100),
    invoice_count INTEGER,
    successful_invoice_count INTEGER,
    failed_invoice_count INTEGER,
    opened_at TIMESTAMP,
    closed_at TIMESTAMP,
    error_message VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_session_tenant ON ksef_sessions(tenant_id);
CREATE INDEX idx_session_reference ON ksef_sessions(reference_number);
CREATE INDEX idx_session_status ON ksef_sessions(status);

-- Invoices table
CREATE TABLE invoices (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    invoice_number VARCHAR(100) NOT NULL,
    ksef_number VARCHAR(100) UNIQUE,
    reference_number VARCHAR(100),
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    invoice_date DATE NOT NULL,
    sale_date DATE NOT NULL,
    seller_nip VARCHAR(10) NOT NULL,
    seller_name VARCHAR(200) NOT NULL,
    buyer_nip VARCHAR(10) NOT NULL,
    buyer_name VARCHAR(200) NOT NULL,
    net_amount DECIMAL(19, 2) NOT NULL,
    vat_amount DECIMAL(19, 2) NOT NULL,
    gross_amount DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'PLN',
    xml_content TEXT,
    upo_content TEXT,
    qr_code VARCHAR(500),
    error_message VARCHAR(1000),
    sent_to_ksef_at TIMESTAMP,
    accepted_by_ksef_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_invoice_tenant ON invoices(tenant_id);
CREATE INDEX idx_invoice_ksef_number ON invoices(ksef_number);
CREATE INDEX idx_invoice_number ON invoices(invoice_number);
CREATE INDEX idx_invoice_status ON invoices(status);
CREATE INDEX idx_invoice_date ON invoices(invoice_date);

-- Audit Logs table
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT REFERENCES tenants(id) ON DELETE SET NULL,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    action_type VARCHAR(50) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(100),
    description VARCHAR(200),
    details TEXT,
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_tenant ON audit_logs(tenant_id);
CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_created ON audit_logs(created_at);
CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Triggers for updated_at
CREATE TRIGGER update_tenants_updated_at BEFORE UPDATE ON tenants
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_certificates_updated_at BEFORE UPDATE ON certificates
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_invoices_updated_at BEFORE UPDATE ON invoices
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
