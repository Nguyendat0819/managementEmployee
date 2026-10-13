-- Seed danh sách chức danh để API /api/role/positions trả dữ liệu cho form nhân viên.
INSERT INTO hr_service.positions (
    position_code,
    title,
    level,
    base_salary_min,
    base_salary_max,
    created_at,
    created_by,
    updated_at,
    updated_by,
    is_deleted
)
SELECT seed.position_code,
       seed.title,
       seed.level,
       seed.base_salary_min,
       seed.base_salary_max,
       CURRENT_TIMESTAMP,
       'system',
       CURRENT_TIMESTAMP,
       'system',
       FALSE
FROM (
    VALUES
        ('INTERN', 'Thực tập sinh', 'ENTRY', 3000000, 6000000),
        ('STAFF', 'Nhân viên', 'JUNIOR', 7000000, 12000000),
        ('SPECIALIST', 'Chuyên viên', 'MIDDLE', 10000000, 18000000),
        ('SENIOR_SPECIALIST', 'Chuyên viên cao cấp', 'SENIOR', 16000000, 28000000),
        ('TEAM_LEAD', 'Trưởng nhóm', 'LEAD', 18000000, 30000000),
        ('DEPUTY_MANAGER', 'Phó phòng', 'MANAGER', 22000000, 38000000),
        ('MANAGER', 'Trưởng phòng', 'MANAGER', 28000000, 50000000),
        ('DEPUTY_DIRECTOR', 'Phó giám đốc', 'DIRECTOR', 40000000, 70000000),
        ('DIRECTOR', 'Giám đốc', 'DIRECTOR', 55000000, 100000000),
        ('SOFTWARE_ENGINEER', 'Kỹ sư phần mềm', 'MIDDLE', 15000000, 30000000),
        ('ACCOUNTANT', 'Kế toán viên', 'MIDDLE', 10000000, 20000000),
        ('HR_SPECIALIST', 'Chuyên viên nhân sự', 'MIDDLE', 10000000, 20000000)
) AS seed(position_code, title, level, base_salary_min, base_salary_max)
WHERE NOT EXISTS (
    SELECT 1
    FROM hr_service.positions existing
    WHERE existing.position_code = seed.position_code
      AND existing.is_deleted = FALSE
);
