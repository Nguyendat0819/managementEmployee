CREATE TABLE IF NOT EXISTS hr_service.menus (
    id BIGSERIAL PRIMARY KEY,
    menu_code VARCHAR(100) NOT NULL UNIQUE,
    label VARCHAR(255) NOT NULL,
    icon VARCHAR(100) NOT NULL,
    icon_color VARCHAR(20),
    path VARCHAR(255),
    parent_code VARCHAR(100) REFERENCES hr_service.menus(menu_code),
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_menus_parent ON hr_service.menus(parent_code);

CREATE TABLE IF NOT EXISTS hr_service.role_menus (
    role_code VARCHAR(255) NOT NULL,
    menu_code VARCHAR(100) NOT NULL REFERENCES hr_service.menus(menu_code),
    PRIMARY KEY (role_code, menu_code)
);

INSERT INTO hr_service.menus (menu_code, label, icon, icon_color, path, parent_code, sort_order)
VALUES
    ('ess', 'Nhân viên', 'user', '#1677ff', NULL, NULL, 10),
    ('notifications', 'Hộp thông báo', 'bell', '#faad14', '/user/notifications', NULL, 20),
    ('admin', 'Quản lý nhân sự', 'setting', '#722ed1', NULL, NULL, 30),
    ('admin-employees', 'Hồ sơ Nhân sự & Hợp đồng', 'team', '#2f54eb', NULL, 'admin', 30),
    ('admin-attendance', 'Phân ca, Lịch nghỉ & Chấm công', 'schedule', '#fa8c16', NULL, 'admin', 40),
    ('admin-leaves', 'Quản lý nghỉ phép', 'audit', '#eb2f96', NULL, 'admin', 50),
    ('admin-payroll', 'Quản lý tiền lương', 'dollar', '#52c41a', NULL, 'admin', 60),
    ('admin-system', 'Tài khoản & phân quyền', 'safety', '#f5222d', NULL, 'admin', 70),
    ('ess-dashboard', 'Trang chủ', 'home', '#52c41a', '/user/dashboard', 'ess', 10),
    ('ess-attendance', 'Chấm công & Ca làm việc', 'calendar', '#13c2c2', '/user/attendance', 'ess', 20),
    ('ess-tasks', 'Công việc được giao', 'ordered-list', '#13c2c2', '/user/tasks', 'ess', 25),
    ('ess-leaves', 'Nghỉ phép cá nhân', 'solution', '#722ed1', '/user/leaves', 'ess', 30),
    ('ess-profile', 'Hồ sơ & Hợp đồng', 'idcard', '#2f54eb', '/user/profile', 'ess', 40),
    ('ess-payslips', 'Phiếu lương điện tử', 'file-text', '#597ef7', '/user/payslips', 'ess', 50),
    ('admin-dashboard', 'Tổng quan nhân sự', 'dashboard', '#1677ff', '/admin/dashboard', 'admin', 10),
    ('admin-organization', 'Cơ cấu tổ chức & Phòng ban', 'apartment', '#13c2c2', '/admin/departments', 'admin', 20),
    ('admin-tasks', 'Quản lý công việc & thưởng', 'ordered-list', '#13c2c2', '/admin/tasks', 'admin', 5),
    ('admin-notifications', 'Gửi thông báo', 'bell', '#faad14', '/admin/notifications', 'admin', 55),
    ('admin-employee-list', 'Danh sách nhân viên', 'team', '#2f54eb', '/admin/employees', 'admin-employees', 10),
    ('admin-contracts', 'Hợp đồng lao động', 'file-text', '#597ef7', '/admin/contracts', 'admin-employees', 20),
    ('admin-positions', 'Danh mục chức vụ', 'solution', '#13c2c2', '/admin/positions', 'admin-employees', 30),
    ('admin-shifts', 'Ca làm việc', 'clock-circle', '#fa8c16', '/admin/shifts', 'admin-attendance', 10),
    ('admin-shift-roster', 'Phân ca làm việc', 'schedule', '#fa8c16', '/admin/shift-roster', 'admin-attendance', 20),
    ('admin-holidays', 'Ngày nghỉ lễ', 'calendar', '#13c2c2', '/admin/holidays', 'admin-attendance', 30),
    ('admin-attendance-management', 'Quản lý chấm công', 'check-circle', '#52c41a', '/admin/attendance', 'admin-attendance', 40),
    ('admin-leave-requests', 'Duyệt đơn nghỉ phép', 'check', '#52c41a', '/admin/leaves', 'admin-leaves', 10),
    ('admin-leave-settings', 'Cấu hình nghỉ phép', 'setting', '#722ed1', '/admin/leave-settings', 'admin-leaves', 20),
    ('admin-salary-structures', 'Thang bảng lương', 'database', '#722ed1', '/admin/salary-structures', 'admin-payroll', 10),
    ('admin-payroll-runs', 'Kỳ tính lương', 'calculator', '#13c2c2', '/admin/payroll-runs', 'admin-payroll', 20),
    ('admin-users', 'Tài khoản người dùng', 'user', '#1677ff', '/admin/users', 'admin-system', 10),
    ('admin-roles-permissions', 'Vai trò & Phân quyền', 'lock', '#f5222d', '/admin/roles-permissions', 'admin-system', 20),
    ('admin-menus', 'Quản lý menu', 'menu', '#1677ff', '/admin/menus', 'admin-system', 25),
    ('admin-audit-logs', 'Nhật ký hoạt động', 'file-search', '#8c8c8c', '/admin/audit-logs', 'admin-system', 30)
ON CONFLICT (menu_code) DO NOTHING;

INSERT INTO hr_service.role_menus (role_code, menu_code)
SELECT r.role_code, m.menu_code
FROM hr_service.roles r
CROSS JOIN hr_service.menus m
WHERE r.is_deleted = false
  AND m.is_deleted = false
  AND m.path IS NOT NULL
  AND (upper(r.role_code) = 'ADMIN' OR
       (upper(r.role_code) IN ('USER', 'EMPLOYEE') AND m.path LIKE '/user/%'))
ON CONFLICT DO NOTHING;
