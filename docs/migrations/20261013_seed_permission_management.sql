-- Seed the permissions used by the employee-service management APIs.
-- Keep this migration idempotent because environments may already contain part of the catalog.
INSERT INTO hr_service.permissions
    (permission_code, permission_name, description, is_deleted, created_at, updated_at)
VALUES
    ('employee:employee:read', 'Xem nhân viên', 'Xem danh sách và chi tiết nhân viên', false, now(), now()),
    ('employee:employee:search', 'Tìm kiếm nhân viên', 'Tìm kiếm nhân viên có phân trang', false, now(), now()),
    ('employee:employee:create', 'Thêm nhân viên', 'Tạo nhân viên mới', false, now(), now()),
    ('employee:employee:update', 'Sửa nhân viên', 'Cập nhật thông tin nhân viên', false, now(), now()),
    ('employee:employee:delete', 'Xóa nhân viên', 'Xóa mềm nhân viên', false, now(), now()),
    ('employee:organization:read', 'Xem đơn vị tổ chức', 'Xem danh sách phòng ban', false, now(), now()),
    ('employee:organization:create', 'Thêm đơn vị tổ chức', 'Tạo phòng ban mới', false, now(), now()),
    ('employee:organization:update', 'Sửa đơn vị tổ chức', 'Cập nhật phòng ban', false, now(), now()),
    ('employee:organization:delete', 'Xóa đơn vị tổ chức', 'Xóa mềm phòng ban', false, now(), now()),
    ('employee:organization:employees-read', 'Xem nhân sự theo đơn vị', 'Xem nhân sự thuộc phòng ban', false, now(), now()),
    ('employee:position:read', 'Xem chức vụ', 'Xem chi tiết chức vụ', false, now(), now()),
    ('employee:position:search', 'Tìm kiếm chức vụ', 'Tìm kiếm chức vụ có phân trang', false, now(), now()),
    ('employee:position:create', 'Thêm chức vụ', 'Tạo chức vụ mới', false, now(), now()),
    ('employee:position:update', 'Sửa chức vụ', 'Cập nhật chức vụ', false, now(), now()),
    ('employee:position:delete', 'Xóa chức vụ', 'Xóa mềm chức vụ', false, now(), now()),
    ('employee:role:read', 'Xem vai trò', 'Xem chi tiết vai trò', false, now(), now()),
    ('employee:role:search', 'Tìm kiếm vai trò', 'Tìm kiếm vai trò có phân trang', false, now(), now()),
    ('employee:role:create', 'Thêm vai trò', 'Tạo vai trò mới', false, now(), now()),
    ('employee:role:update', 'Sửa vai trò', 'Cập nhật vai trò', false, now(), now()),
    ('employee:role:delete', 'Xóa vai trò', 'Xóa mềm vai trò', false, now(), now()),
    ('employee:role:positions-read', 'Xem chức danh của vai trò', 'Xem danh sách chức danh để gán cho vai trò', false, now(), now()),
    ('employee:user:read', 'Xem tài khoản', 'Xem danh sách tài khoản', false, now(), now()),
    ('employee:user:status-update', 'Đổi trạng thái tài khoản', 'Kích hoạt hoặc khóa tài khoản', false, now(), now()),
    ('employee:role-permission:read', 'Xem phân quyền', 'Xem quyền đã gán theo vai trò', false, now(), now()),
    ('employee:role-permission:assign', 'Gán quyền', 'Gán thêm một quyền cho vai trò', false, now(), now()),
    ('employee:role-permission:update', 'Cập nhật phân quyền', 'Cập nhật toàn bộ danh sách quyền của vai trò', false, now(), now()),
    ('employee:role-permission:delete', 'Xóa phân quyền', 'Gỡ một quyền khỏi vai trò', false, now(), now()),
    ('employee:permission:read', 'Xem danh mục quyền', 'Tìm kiếm và xem chi tiết quyền', false, now(), now()),
    ('employee:permission:create', 'Thêm quyền', 'Tạo quyền mới trong danh mục quyền', false, now(), now()),
    ('employee:permission:update', 'Sửa quyền', 'Cập nhật tên và mô tả quyền', false, now(), now()),
    ('employee:permission:delete', 'Xóa quyền', 'Xóa mềm quyền và gỡ khỏi các vai trò', false, now(), now())
ON CONFLICT (permission_code) DO UPDATE
    SET permission_name = EXCLUDED.permission_name,
        description = EXCLUDED.description,
        is_deleted = false,
        updated_at = now();

-- ADMIN is also allowed by PermissionChecker, but storing the assignments keeps the
-- database and the in-memory RolePermissionCache consistent for internal consumers.
INSERT INTO hr_service.role_permissions
    (role_code, permission_code, is_deleted, created_at, updated_at)
SELECT 'ADMIN', p.permission_code, false, now(), now()
  FROM hr_service.permissions p
 WHERE p.permission_code LIKE 'employee:%'
   AND p.is_deleted = false
   AND EXISTS (SELECT 1 FROM hr_service.roles r WHERE r.role_code = 'ADMIN')
ON CONFLICT (role_code, permission_code) DO UPDATE
    SET is_deleted = false,
        updated_at = now();
