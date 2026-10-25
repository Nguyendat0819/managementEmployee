-- Add role permissions for employee task tracking and payroll bonus review.
INSERT INTO hr_service.permissions
    (permission_code, permission_name, description, is_deleted, created_at, updated_at)
VALUES
    ('employee:task:read', 'Xem task cá nhân', 'Xem, bắt đầu và nộp task được giao', false, now(), now()),
    ('employee:task:manage', 'Quản lý task', 'Giao task, xem tiến độ và duyệt thưởng task', false, now(), now())
ON CONFLICT (permission_code) DO UPDATE
    SET permission_name = EXCLUDED.permission_name,
        description = EXCLUDED.description,
        is_deleted = false,
        updated_at = now();

INSERT INTO hr_service.role_permissions
    (role_code, permission_code, is_deleted, created_at, updated_at)
SELECT role.role_code, permission.permission_code, false, now(), now()
  FROM hr_service.roles role
 CROSS JOIN hr_service.permissions permission
 WHERE (role.role_code = 'USER' AND permission.permission_code = 'employee:task:read')
    OR (role.role_code = 'ADMIN' AND permission.permission_code IN ('employee:task:read', 'employee:task:manage'))
ON CONFLICT (role_code, permission_code) DO UPDATE
    SET is_deleted = false,
        updated_at = now();
