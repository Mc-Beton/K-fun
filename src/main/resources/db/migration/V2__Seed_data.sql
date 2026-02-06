-- V2__Seed_data.sql
-- Initial seed data for development

-- Insert test tenant
INSERT INTO tenants (nip, name, full_name, email, phone, address, active, status)
VALUES ('1234567890', 'Test Company', 'Test Company Sp. z o.o.', 'contact@testcompany.pl', '+48123456789', 
        'ul. Testowa 1, 00-000 Warszawa', true, 'ACTIVE');

-- Insert admin user (password: Admin123!)
-- Password hash generated with BCrypt
INSERT INTO users (email, first_name, last_name, password_hash, tenant_id, role, active, email_verified)
VALUES ('admin@testcompany.pl', 'Admin', 'User', 
        '$2a$10$N.zmdr5Zc4q3H3yl3xYQiu7nT9s8pKZ5xN7zXjLfHX4wPGV8K5Hhy',
        (SELECT id FROM tenants WHERE nip = '1234567890'),
        'ADMIN', true, true);
