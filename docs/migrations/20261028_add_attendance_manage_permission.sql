BEGIN;

INSERT INTO hr_service.permissions
    (permission_code, permission_name, description, is_deleted, created_at, updated_at)
VALUES
    ('employee:attendance:manage', 'Quản lý chấm công', 'Xem toàn bộ bản ghi chấm công và quản lý dữ liệu chấm công', false, now(), now())
ON CONFLICT (permission_code) DO UPDATE
    SET permission_name = EXCLUDED.permission_name,
        description = EXCLUDED.description,
        is_deleted = false,
        updated_at = now();

INSERT INTO hr_service.role_permissions
    (role_code, permission_code, is_deleted, created_at, updated_at)
SELECT roles.role_code, 'employee:attendance:manage', false, now(), now()
  FROM hr_service.roles roles
 WHERE upper(roles.role_code) IN ('ADMIN', 'HR')
ON CONFLICT (role_code, permission_code) DO UPDATE
    SET is_deleted = false,
        updated_at = now();

COMMIT;
