package com.example.employee_service.feature.constant;

/**
 * Định nghĩa permission code cho từng action nghiệp vụ.
 *
 * <p>Quy tắc đặt tên: {@code <tính-năng>:<đối-tượng>:<action>}. Tên cũng chính là
 * giá trị truyền cho annotation {@code @RequiresPermission}.</p>
 *
 * <p>Ví dụ:</p>
 * <pre>
 *   &#64;RequiresPermission(PermissionDefine.EMPLOYEE_READ)
 * </pre>
 */
public final class PermissionDefine {

    private PermissionDefine() {
    }

    public static final String EMPLOYEE_READ = "employee:employee:read";
    public static final String EMPLOYEE_SEARCH = "employee:employee:search";
    public static final String EMPLOYEE_CREATE = "employee:employee:create";
    public static final String EMPLOYEE_UPDATE = "employee:employee:update";
    public static final String EMPLOYEE_DELETE = "employee:employee:delete";

    public static final String CONTRACT_READ = "employee:contract:read";
    public static final String CONTRACT_SEARCH = "employee:contract:search";
    public static final String CONTRACT_CREATE = "employee:contract:create";
    public static final String CONTRACT_UPDATE = "employee:contract:update";
    public static final String CONTRACT_DELETE = "employee:contract:delete";

    public static final String ORGANIZATION_READ = "employee:organization:read";
    public static final String ORGANIZATION_CREATE = "employee:organization:create";
    public static final String ORGANIZATION_UPDATE = "employee:organization:update";
    public static final String ORGANIZATION_DELETE = "employee:organization:delete";
    public static final String ORGANIZATION_EMPLOYEES_READ = "employee:organization:employees-read";

    public static final String POSITION_READ = "employee:position:read";
    public static final String POSITION_SEARCH = "employee:position:search";
    public static final String POSITION_CREATE = "employee:position:create";
    public static final String POSITION_UPDATE = "employee:position:update";
    public static final String POSITION_DELETE = "employee:position:delete";

    public static final String ROLE_READ = "employee:role:read";
    public static final String ROLE_SEARCH = "employee:role:search";
    public static final String ROLE_CREATE = "employee:role:create";
    public static final String ROLE_UPDATE = "employee:role:update";
    public static final String ROLE_DELETE = "employee:role:delete";
    public static final String ROLE_POSITIONS_READ = "employee:role:positions-read";

    public static final String USER_READ = "employee:user:read";
    public static final String USER_STATUS_UPDATE = "employee:user:status-update";

    public static final String ROLE_PERMISSION_READ = "employee:role-permission:read";
    public static final String ROLE_PERMISSION_ASSIGN = "employee:role-permission:assign";
    public static final String ROLE_PERMISSION_UPDATE = "employee:role-permission:update";
    public static final String ROLE_PERMISSION_DELETE = "employee:role-permission:delete";

    public static final String PERMISSION_READ = "employee:permission:read";
    public static final String PERMISSION_CREATE = "employee:permission:create";
    public static final String PERMISSION_UPDATE = "employee:permission:update";
    public static final String PERMISSION_DELETE = "employee:permission:delete";
}
