BEGIN;

CREATE SCHEMA IF NOT EXISTS hr_service;

CREATE TABLE IF NOT EXISTS hr_service.audit_logs (
    id BIGSERIAL PRIMARY KEY,
    "user" BIGINT,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    old_value JSONB,
    new_value JSONB,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    request_uri VARCHAR(500),
    result VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

ALTER TABLE hr_service.audit_logs ADD COLUMN IF NOT EXISTS "user" BIGINT;
ALTER TABLE hr_service.audit_logs ADD COLUMN IF NOT EXISTS action VARCHAR(50);
ALTER TABLE hr_service.audit_logs ADD COLUMN IF NOT EXISTS entity_type VARCHAR(50);
ALTER TABLE hr_service.audit_logs ADD COLUMN IF NOT EXISTS entity_id BIGINT;
ALTER TABLE hr_service.audit_logs ADD COLUMN IF NOT EXISTS old_value JSONB;
ALTER TABLE hr_service.audit_logs ADD COLUMN IF NOT EXISTS new_value JSONB;
ALTER TABLE hr_service.audit_logs ADD COLUMN IF NOT EXISTS ip_address VARCHAR(45);
ALTER TABLE hr_service.audit_logs ADD COLUMN IF NOT EXISTS user_agent VARCHAR(500);
ALTER TABLE hr_service.audit_logs ADD COLUMN IF NOT EXISTS request_uri VARCHAR(500);
ALTER TABLE hr_service.audit_logs ADD COLUMN IF NOT EXISTS result VARCHAR(20) NOT NULL DEFAULT 'SUCCESS';
ALTER TABLE hr_service.audit_logs ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE hr_service.audit_logs ALTER COLUMN entity_id DROP NOT NULL;

CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON hr_service.audit_logs (created_at DESC, id DESC);
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity_type ON hr_service.audit_logs (entity_type, result);

INSERT INTO hr_service.permissions
    (permission_code, permission_name, description, is_deleted, created_at, updated_at)
VALUES
    ('employee:audit-log:read', 'Xem nhật ký hệ thống', 'Tra cứu lịch sử thao tác quản trị và sự kiện bảo mật', false, now(), now())
ON CONFLICT (permission_code) DO UPDATE
    SET permission_name = EXCLUDED.permission_name,
        description = EXCLUDED.description,
        is_deleted = false,
        updated_at = now();

INSERT INTO hr_service.role_permissions
    (role_code, permission_code, is_deleted, created_at, updated_at)
SELECT r.role_code, 'employee:audit-log:read', false, now(), now()
  FROM hr_service.roles r
 WHERE upper(r.role_code) = 'ADMIN'
ON CONFLICT (role_code, permission_code) DO UPDATE
    SET is_deleted = false,
        updated_at = now();

COMMIT;
