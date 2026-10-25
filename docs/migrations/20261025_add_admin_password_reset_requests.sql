CREATE TABLE IF NOT EXISTS hr_service.password_reset_requests (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES hr_service.users(id) ON DELETE CASCADE,
    requested_at TIMESTAMP NOT NULL DEFAULT now(),
    resolved_at TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_password_reset_requests_pending
    ON hr_service.password_reset_requests (requested_at)
    WHERE status = 'PENDING';

DROP TABLE IF EXISTS hr_service.password_reset_tokens;
