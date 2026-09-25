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

    // EmployeeApi (mẫu)
    public static final String EMPLOYEE_READ = "employee:employee:read";
    public static final String EMPLOYEE_TEST = "employee:employee:test";
}