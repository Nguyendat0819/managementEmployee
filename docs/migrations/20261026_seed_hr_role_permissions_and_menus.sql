BEGIN;

-- Keep HR operational; account, permission, menu administration, and audit menus remain ADMIN-only.
INSERT INTO hr_service.roles
    (role_code, role_name, description, is_deleted, created_at, updated_at)
SELECT 'HR', 'Nhân sự', 'Quản lý nghiệp vụ nhân sự', false, now(), now()
WHERE NOT EXISTS (
    SELECT 1
      FROM hr_service.roles
     WHERE upper(role_code) = 'HR'
);

UPDATE hr_service.roles
   SET role_name = 'Nhân sự',
       description = 'Quản lý nghiệp vụ nhân sự',
       is_deleted = false,
       updated_at = now()
 WHERE upper(role_code) = 'HR';

WITH hr_permissions(permission_code) AS (
    VALUES
        ('employee:employee:read'),
        ('employee:employee:search'),
        ('employee:employee:create'),
        ('employee:employee:update'),
        ('employee:contract:read'),
        ('employee:contract:search'),
        ('employee:contract:create'),
        ('employee:contract:update'),
        ('employee:organization:read'),
        ('employee:organization:create'),
        ('employee:organization:update'),
        ('employee:organization:employees-read'),
        ('employee:position:read'),
        ('employee:position:search'),
        ('employee:position:create'),
        ('employee:position:update'),
        ('employee:shift:read'),
        ('employee:shift:manage'),
        ('employee:leave:read'),
        ('employee:leave:approve'),
        ('employee:leave:manage'),
        ('employee:notification:read'),
        ('employee:notification:create'),
        ('employee:task:read'),
        ('employee:task:manage'),
        ('employee:salary:manage'),
        ('employee:payroll:read'),
        ('employee:payroll:manage')
)
UPDATE hr_service.role_permissions rp
   SET is_deleted = true,
       updated_at = now()
 WHERE upper(rp.role_code) = 'HR'
   AND rp.is_deleted = false
   AND NOT EXISTS (
       SELECT 1
         FROM hr_permissions hp
        WHERE hp.permission_code = rp.permission_code
   );

WITH hr_permissions(permission_code) AS (
    VALUES
        ('employee:employee:read'),
        ('employee:employee:search'),
        ('employee:employee:create'),
        ('employee:employee:update'),
        ('employee:contract:read'),
        ('employee:contract:search'),
        ('employee:contract:create'),
        ('employee:contract:update'),
        ('employee:organization:read'),
        ('employee:organization:create'),
        ('employee:organization:update'),
        ('employee:organization:employees-read'),
        ('employee:position:read'),
        ('employee:position:search'),
        ('employee:position:create'),
        ('employee:position:update'),
        ('employee:shift:read'),
        ('employee:shift:manage'),
        ('employee:leave:read'),
        ('employee:leave:approve'),
        ('employee:leave:manage'),
        ('employee:notification:read'),
        ('employee:notification:create'),
        ('employee:task:read'),
        ('employee:task:manage'),
        ('employee:salary:manage'),
        ('employee:payroll:read'),
        ('employee:payroll:manage')
)
INSERT INTO hr_service.role_permissions
    (role_code, permission_code, is_deleted, created_at, updated_at)
SELECT 'HR', p.permission_code, false, now(), now()
  FROM hr_permissions p
  JOIN hr_service.permissions catalog
    ON catalog.permission_code = p.permission_code
   AND catalog.is_deleted = false
ON CONFLICT (role_code, permission_code) DO UPDATE
    SET is_deleted = false,
        updated_at = now();

DELETE FROM hr_service.role_menus
 WHERE upper(role_code) = 'HR';

INSERT INTO hr_service.role_menus (role_code, menu_code)
SELECT 'HR', menu.menu_code
  FROM hr_service.menus menu
 WHERE menu.is_deleted = false
   AND menu.menu_code IN (
       'notifications',
       'ess-dashboard',
       'ess-attendance',
       'ess-tasks',
       'ess-leaves',
       'ess-profile',
       'ess-payslips',
       'admin-dashboard',
       'admin-organization',
       'admin-tasks',
       'admin-employee-list',
       'admin-contracts',
       'admin-positions',
       'admin-shifts',
       'admin-shift-roster',
       'admin-holidays',
       'admin-attendance-management',
       'admin-leave-requests',
       'admin-leave-settings',
       'admin-salary-structures',
       'admin-payroll-runs',
       'admin-notifications'
   )
ON CONFLICT DO NOTHING;

COMMIT;
