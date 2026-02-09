-- Tabela powiadomień systemowych
CREATE TABLE system_notifications (
    id BIGSERIAL PRIMARY KEY,
    category VARCHAR(50) NOT NULL,
    level VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    details TEXT,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP
);

-- Indeksy dla wydajności
CREATE INDEX idx_notifications_category ON system_notifications(category);
CREATE INDEX idx_notifications_level ON system_notifications(level);
CREATE INDEX idx_notifications_is_read ON system_notifications(is_read);
CREATE INDEX idx_notifications_created_at ON system_notifications(created_at DESC);

-- Seed: Powiadomienie o uruchomieniu systemu
INSERT INTO system_notifications (category, level, title, message, details, is_read, created_at)
VALUES (
    'HUB',
    'SUCCESS',
    'System uruchomiony',
    'KSeF Hub został pomyślnie uruchomiony i jest gotowy do pracy.',
    '{"version": "1.0.0", "profile": "h2", "startup_time_ms": 5432}',
    false,
    CURRENT_TIMESTAMP
);
