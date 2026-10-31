from pathlib import Path
from PIL import Image, ImageDraw, ImageFont


OUT = Path(__file__).resolve().parent
BLUE = "#22519B"
INK = "#17233B"
MUTED = "#61718B"
LINE = "#A9BDD9"
PALE = "#F4F8FD"
RED = "#BE3B35"
TEAL = "#0E7189"
FONT = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
BOLD = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"


def font(size, bold=False):
    return ImageFont.truetype(BOLD if bold else FONT, size)


TABLES = {
    "users": [("id", "BIGINT", "pk"), ("user_name", "VARCHAR", ""), ("role_code", "VARCHAR", "ref"), ("status", "VARCHAR", "")],
    "employees": [("id", "BIGINT", "pk"), ("employee_code", "VARCHAR", "key"), ("user_id", "BIGINT", "ref"), ("department_code", "VARCHAR", "ref"), ("position_code", "VARCHAR", "ref"), ("status", "VARCHAR", "")],
    "departments": [("id", "BIGINT", "pk"), ("dept_code", "VARCHAR", "key"), ("parent_code", "VARCHAR", "ref"), ("name", "VARCHAR", "")],
    "positions": [("id", "BIGINT", "pk"), ("position_code", "VARCHAR", "key"), ("title", "VARCHAR", "")],
    "employee_contracts": [("id", "BIGINT", "pk"), ("contract_no", "VARCHAR", "key"), ("employee_code", "VARCHAR", "ref"), ("start_date", "DATE", ""), ("end_date", "DATE", ""), ("actual_salary", "DECIMAL", "")],
    "password_reset_requests": [("id", "BIGINT", "pk"), ("user_id", "BIGINT", "fk"), ("requested_at", "TIMESTAMP", ""), ("status", "VARCHAR", "")],
    "audit_logs": [("id", "BIGINT", "pk"), ("user", "BIGINT", "ref"), ("entity_type", "VARCHAR", ""), ("entity_id", "BIGINT", ""), ("action", "VARCHAR", "")],
    "roles": [("id", "BIGINT", "pk"), ("role_code", "VARCHAR", "key"), ("role_name", "VARCHAR", "")],
    "permissions": [("id", "BIGINT", "pk"), ("permission_code", "VARCHAR", "key"), ("permission_name", "VARCHAR", "")],
    "role_permissions": [("id", "BIGINT", "pk"), ("role_code", "VARCHAR", "ref"), ("permission_code", "VARCHAR", "ref")],
    "menus": [("id", "BIGINT", "pk"), ("menu_code", "VARCHAR", "key"), ("parent_code", "VARCHAR", "fk"), ("path", "VARCHAR", "")],
    "role_menus": [("role_code", "VARCHAR", "pk"), ("menu_code", "VARCHAR", "pk")],
    "shifts": [("id", "BIGINT", "pk"), ("shift_name", "VARCHAR", ""), ("start_time", "TIME", ""), ("end_time", "TIME", ""), ("day_of_week", "JSONB", "")],
    "employee_shift_assignments": [("id", "BIGINT", "pk"), ("employee_code", "VARCHAR", "ref"), ("shift", "BIGINT", "ref"), ("effective_date", "DATE", ""), ("end_date", "DATE", "")],
    "attendance_records": [("id", "BIGINT", "pk"), ("employee_code", "VARCHAR", "ref"), ("shift_id", "BIGINT", "ref"), ("recorded_at", "DATE", ""), ("check_in", "TIMESTAMP", ""), ("check_out", "TIMESTAMP", ""), ("status", "VARCHAR", "")],
    "leave_types": [("id", "BIGINT", "pk"), ("code", "VARCHAR", "key"), ("max_days_per_year", "INT", ""), ("carryover_days", "INT", "")],
    "leave_requests": [("id", "BIGINT", "pk"), ("request_code", "VARCHAR", "key"), ("employee_code", "VARCHAR", "ref"), ("type", "BIGINT", "ref"), ("start_date", "DATE", ""), ("end_date", "DATE", ""), ("status", "VARCHAR", "")],
    "leave_balances": [("id", "BIGINT", "pk"), ("employee_code", "VARCHAR", "ref"), ("type", "BIGINT", "ref"), ("balance_year", "INT", ""), ("total_days", "DECIMAL", ""), ("used_days", "DECIMAL", "")],
    "holidays": [("id", "BIGINT", "pk"), ("holiday_date", "DATE", ""), ("holiday_date_end", "DATE", ""), ("name", "VARCHAR", "")],
    "salary_structures": [("id", "BIGINT", "pk"), ("position_code", "VARCHAR", "ref"), ("base_salary", "DECIMAL", ""), ("effective_date", "DATE", ""), ("formula_config", "JSONB", "")],
    "payroll_runs": [("id", "BIGINT", "pk"), ("period_year", "SMALLINT", ""), ("period_month", "INT", ""), ("status", "VARCHAR", ""), ("total_net", "DECIMAL", "")],
    "payslips": [("id", "BIGINT", "pk"), ("run", "BIGINT", "ref"), ("employee_code", "VARCHAR", "ref"), ("gross_salary", "DECIMAL", ""), ("net_salary", "DECIMAL", ""), ("snapshot_data", "JSONB", "")],
    "payroll_holidays": [("id", "BIGINT", "pk"), ("holiday_date", "DATE", ""), ("holiday_date_end", "DATE", ""), ("name", "VARCHAR", "")],
    "employee_tasks": [("id", "BIGINT", "pk"), ("employee_code", "VARCHAR", "ref"), ("title", "VARCHAR", ""), ("period_month", "DATE", ""), ("status", "VARCHAR", "")],
    "employee_task_progress_submissions": [("id", "BIGINT", "pk"), ("task_id", "BIGINT", "fk"), ("result", "VARCHAR", ""), ("status", "VARCHAR", ""), ("submitted_at", "TIMESTAMP", "")],
    "notifications": [("id", "BIGINT", "pk"), ("user", "BIGINT", "ref"), ("channel", "VARCHAR", ""), ("title", "VARCHAR", ""), ("is_read", "BOOLEAN", ""), ("reference_id", "BIGINT", "")],
}


def header_spec(draw, name, width, mini):
    size = 21 if mini else 26
    if draw.textlength(name, font=font(size, True)) <= width - 25:
        return [name], 38 if mini else 46, size
    parts = name.split("_")
    splits = [("_".join(parts[:i]), "_".join(parts[i:])) for i in range(1, len(parts))]
    first, second = min(splits, key=lambda pair: max(draw.textlength(pair[0], font=font(19 if mini else 22, True)), draw.textlength(pair[1], font=font(19 if mini else 22, True))))
    size = 19 if mini else 22
    while max(draw.textlength(first, font=font(size, True)), draw.textlength(second, font=font(size, True))) > width - 25:
        size -= 1
    return [first, second], 62 if mini else 70, size


def table_box(draw, name, x, y, w=345, external=False, mini=False):
    fields = TABLES[name][:2] if mini else TABLES[name]
    lines, header, title_size = header_spec(draw, name, w, mini)
    step = 27 if mini else 31
    h = header + len(fields) * step + (12 if mini else 18)
    draw.rounded_rectangle((x, y, x + w, y + h), radius=8, fill="white", outline=LINE, width=2)
    draw.rounded_rectangle((x, y, x + w, y + header), radius=8, fill="#71849F" if external else BLUE)
    draw.rectangle((x, y + header - 8, x + w, y + header), fill="#71849F" if external else BLUE)
    for index, line in enumerate(lines):
        draw.text((x + 13, y + (6 if mini else 8) + index * (25 if mini else 31)), line, font=font(title_size, True), fill="white")
    for i, (field, typ, kind) in enumerate(fields):
        yy = y + header + 7 + i * step
        color = RED if kind == "pk" else BLUE if kind == "fk" else TEAL if kind in ("ref", "key") else INK
        draw.text((x + 13, yy), field, font=font(17 if mini else 21), fill=color)
        if not mini:
            fw = draw.textlength(typ, font=font(18))
            draw.text((x + w - 13 - fw, yy + 1), typ, font=font(18), fill=MUTED)
    return (x, y, x + w, y + h)


def dash_line(draw, points, color, width=3, dash=13, gap=9):
    import math
    for (x1, y1), (x2, y2) in zip(points, points[1:]):
        dx, dy = x2 - x1, y2 - y1
        length = math.hypot(dx, dy)
        if not length:
            continue
        pos = 0
        while pos < length:
            end = min(pos + dash, length)
            draw.line((x1 + dx * pos / length, y1 + dy * pos / length, x1 + dx * end / length, y1 + dy * end / length), fill=color, width=width)
            pos += dash + gap


def relation(draw, boxes, source, target, actual=False):
    a, b = boxes[source], boxes[target]
    ac = ((a[0] + a[2]) / 2, (a[1] + a[3]) / 2)
    bc = ((b[0] + b[2]) / 2, (b[1] + b[3]) / 2)
    if abs(bc[1] - ac[1]) > 100:
        s = (ac[0], a[3] if bc[1] > ac[1] else a[1])
        e = (bc[0], b[1] if bc[1] > ac[1] else b[3])
        mid = (s[1] + e[1]) / 2
        points = [s, (s[0], mid), (e[0], mid), e]
    else:
        s = (a[2] if bc[0] > ac[0] else a[0], ac[1])
        e = (b[0] if bc[0] > ac[0] else b[2], bc[1])
        mid = (s[0] + e[0]) / 2
        points = [s, (mid, s[1]), (mid, e[1]), e]
    if actual:
        draw.line(points, fill=BLUE, width=4, joint="curve")
    else:
        dash_line(draw, points, "#748AA8")
    draw.ellipse((s[0] - 5, s[1] - 5, s[0] + 5, s[1] + 5), fill=BLUE if actual else "#748AA8")
    draw.ellipse((e[0] - 5, e[1] - 5, e[0] + 5, e[1] + 5), fill=BLUE if actual else "#748AA8")


DETAILS = [
    ("nhan_su", "NHÂN SỰ VÀ TỔ CHỨC", 1130,
     {"departments": (60, 115), "positions": (480, 115), "users": (900, 115), "employees": (480, 430), "employee_contracts": (60, 750), "audit_logs": (480, 750), "password_reset_requests": (900, 750)},
     [("departments", "employees", False), ("positions", "employees", False), ("users", "employees", False), ("employees", "employee_contracts", False), ("users", "password_reset_requests", True), ("users", "audit_logs", False)], set()),
    ("phan_quyen", "VAI TRÒ QUYỀN VÀ MENU", 830,
     {"roles": (60, 115), "users": (480, 115), "permissions": (900, 115), "role_permissions": (60, 465), "menus": (480, 465), "role_menus": (900, 465)},
     [("roles", "users", False), ("roles", "role_permissions", False), ("permissions", "role_permissions", False), ("roles", "role_menus", False), ("menus", "role_menus", True)], set()),
    ("cham_cong", "CA LÀM VÀ CHẤM CÔNG", 830,
     {"shifts": (60, 115), "employees": (900, 115), "employee_shift_assignments": (60, 465), "attendance_records": (900, 465)},
     [("shifts", "employee_shift_assignments", False), ("shifts", "attendance_records", False), ("employees", "employee_shift_assignments", False), ("employees", "attendance_records", False)], {"employees"}),
    ("nghi_phep", "NGHỈ PHÉP", 900,
     {"leave_types": (60, 115), "employees": (480, 115), "holidays": (900, 115), "leave_balances": (60, 465), "leave_requests": (480, 465)},
     [("leave_types", "leave_balances", False), ("leave_types", "leave_requests", False), ("employees", "leave_balances", False), ("employees", "leave_requests", False)], {"employees"}),
    ("luong_cong_viec", "TIỀN LƯƠNG VÀ CÔNG VIỆC", 1130,
     {"employees": (60, 115), "positions": (480, 115), "salary_structures": (900, 115), "employee_tasks": (60, 430), "payroll_runs": (480, 430), "payslips": (900, 430), "employee_task_progress_submissions": (60, 750), "payroll_holidays": (480, 750)},
     [("employees", "employee_tasks", False), ("positions", "salary_structures", False), ("payroll_runs", "payslips", False), ("employees", "payslips", False), ("employee_tasks", "employee_task_progress_submissions", True)], {"employees", "positions"}),
    ("thong_bao_nhat_ky", "THÔNG BÁO VÀ NHẬT KÝ", 830,
     {"users": (480, 115), "notifications": (60, 465), "audit_logs": (900, 465)},
     [("users", "notifications", False), ("users", "audit_logs", False)], {"users", "audit_logs"}),
]


def draw_detail(slug, title, height, positions, edges, external):
    image = Image.new("RGB", (1320, height), "white")
    draw = ImageDraw.Draw(image)
    draw.text((60, 43), title, font=font(34, True), fill=INK)
    draw.text((60, 82), "Schema hr_service  |  các trường chính", font=font(21), fill=MUTED)
    boxes = {}
    for name, (x, y) in positions.items():
        fields = TABLES[name]
        _, header, _ = header_spec(draw, name, 345, False)
        h = header + len(fields) * 31 + 18
        boxes[name] = (x, y, x + 345, y + h)
    for source, target, actual in edges:
        relation(draw, boxes, source, target, actual)
    for name, (x, y) in positions.items():
        table_box(draw, name, x, y, external=name in external)
    footer_y = height - 86
    draw.line((60, footer_y - 14, 1260, footer_y - 14), fill=LINE, width=2)
    draw.text((60, footer_y), "Đỏ: khóa chính   Xanh liền: FK khai báo   Nét đứt: liên kết logic qua mã hoặc ID", font=font(19), fill=MUTED)
    if external:
        note = "Đầu bảng xám: bảng thuộc nhóm khác."
        if slug == "thong_bao_nhat_ky":
            note += " audit_logs mới có entity, chưa xác nhận API."
        draw.text((60, footer_y + 29), note, font=font(18), fill=MUTED)
    image.save(OUT / f"database_{slug}.png", dpi=(220, 220))


GROUPS = [
    ("NHÂN SỰ VÀ PHÂN QUYỀN", (30, 60, 690, 1010), ["users", "employees", "departments", "positions", "employee_contracts", "password_reset_requests", "roles", "permissions", "role_permissions", "menus", "role_menus", "audit_logs"], 2, 6),
    ("CHẤM CÔNG", (705, 60, 1370, 510), ["shifts", "employee_shift_assignments", "attendance_records"], 2, 2),
    ("NGHỈ PHÉP", (705, 520, 1370, 835), ["leave_types", "leave_requests", "leave_balances", "holidays"], 2, 2),
    ("TIỀN LƯƠNG VÀ CÔNG VIỆC", (30, 1020, 1030, 1480), ["salary_structures", "payroll_runs", "payslips", "payroll_holidays", "employee_tasks", "employee_task_progress_submissions"], 3, 2),
    ("THÔNG BÁO", (1045, 1020, 1370, 1220), ["notifications"], 1, 1),
]


def draw_overview():
    image = Image.new("RGB", (1400, 1550), "white")
    draw = ImageDraw.Draw(image)
    draw.text((30, 15), "TỔNG THỂ CƠ SỞ DỮ LIỆU HRM", font=font(34, True), fill=INK)
    for title, (left, top, right, bottom), names, columns, _ in GROUPS:
        draw.rounded_rectangle((left, top, right, bottom), radius=13, fill=PALE, outline=LINE, width=2)
        draw.text((left + 14, top + 13), title, font=font(23, True), fill=BLUE)
        available = right - left - 26
        gap = 14
        width = int((available - gap * (columns - 1)) / columns)
        for index, name in enumerate(names):
            col, row = index % columns, index // columns
            x = left + 13 + col * (width + gap)
            y = top + 55 + row * 144
            table_box(draw, name, x, y, w=width, mini=True)
    draw.text((30, 1510), "26 bảng/khai báo bảng  |  quan hệ chi tiết ở các hình tiếp theo  |  nguồn: JPA entity và SQL migration", font=font(18), fill=MUTED)
    image.save(OUT / "database_tong_the.png", dpi=(220, 220))


if __name__ == "__main__":
    OUT.mkdir(parents=True, exist_ok=True)
    draw_overview()
    for item in DETAILS:
        draw_detail(*item)
