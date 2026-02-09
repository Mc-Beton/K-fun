-- Hub Settings Table
CREATE TABLE IF NOT EXISTS hub_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    processing_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ksef_auto_connect BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(100)
);

-- Insert default settings
INSERT INTO hub_settings (processing_enabled, ksef_auto_connect, updated_by)
VALUES (true, true, 'SYSTEM');
