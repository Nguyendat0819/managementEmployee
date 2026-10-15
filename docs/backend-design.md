# Attendance backend design

## Scope and boundaries

The service owns shift definitions, employee-to-shift assignments, attendance punches, and attendance summaries. Employee and department master data remain owned by `employee_service`; this service stores employee codes and enriches administrative screens through an internal employee-service client when that integration is added. The API gateway verifies the shared JWT, and this service enforces role permissions for administrative operations.

Recommended layers follow the existing project structure:

```text
REST API -> application service (business rules + transaction) -> repositories -> PostgreSQL
                                  |-> employee service (employee/department labels)
```

Controllers should only validate/map HTTP input. Services own attendance state changes and calculations. Repositories persist attendance and query date-range views. Do not store employee names or department labels as attendance facts; they can change over time.

## Data model

| Table | Purpose | Important constraints |
| --- | --- | --- |
| `hr_service.shifts` | Reusable shift definitions: name, start/end, weekdays, active/deleted state | End may be earlier than start for overnight shifts |
| `hr_service.employee_shift_assignments` | Employee code, shift ID, effective date range | No overlapping active assignments for the same employee |
| `hr_service.attendance_records` | One daily attendance record with check-in/out, durations, status, note | Unique `(employee_code, recorded_at)`; dates are evaluated in `Asia/Ho_Chi_Minh` |

Keep `attendance_records` independent of employee master tables to avoid cross-service foreign keys. Assignment rows should refer to a shift ID, and service logic should resolve the assignment effective for the recorded work date. For overnight shifts, `recorded_at` is the shift start date.

## HTTP contract

All responses use the shared `ApiResponse` envelope. Date query values use ISO `yyyy-MM-dd`; timestamps use ISO local date-time in the configured business timezone.

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/attendance/check-in` | Start the caller's assigned shift; reject a second check-in for that shift date |
| `POST` | `/api/attendance/check-out` | Close the caller's latest open record; calculate worked and overtime minutes |
| `GET` | `/api/attendance/history?employeeCode=&from=&to=` | Employee history for the requested date range |
| `GET` | `/api/attendance/records?from=&to=&employeeCode=&status=&page=&size=` | Administrative paginated attendance search |
| `GET/POST/PUT/DELETE` | `/api/shifts` | List, create, update, and deactivate shift definitions |
| `GET/POST/PUT/DELETE` | `/api/shift-assignments` | List and maintain effective-dated employee assignments |
| `GET` | `/api/shift-assignments/current?employeeCode=&date=` | Read the employee's assignment for a date; returns `data: null` when none exists |
| `GET` | `/api/attendance/summary?month=` | Monthly totals: worked days, minutes, late days, overtime |

Shift and assignment management endpoints are implemented; monthly summary is not. Employee self-service should ultimately derive employee code from a trusted JWT claim rather than accept it from the body/query. Shift administration endpoints require `employee:shift:read` or `employee:shift:manage` permission codes.

## Business rules

1. When an active shift assignment applies to the employee and work date, check-in snapshots the shift and compares the actual time with its scheduled start. An unassigned employee can still check in, but no late/early classification is made.
2. Late minutes are `max(0, checkIn - shiftStart)`; early minutes are `max(0, shiftStart - checkIn)`. A configurable grace period is not implemented yet.
3. Current work duration is the elapsed interval between check-in and check-out in whole minutes; unpaid break deductions are not configurable yet. Overtime is calculated only when an active shift snapshot exists and is the elapsed time after its scheduled end. Without a shift snapshot the record is marked `NO_SHIFT` and no overtime is claimed.
4. A missing check-out keeps the current record open; the system must not invent a checkout time. A check-in without an active shift is marked `NO_SHIFT`. Absence generation should run only after the shift's attendance window closes and must exclude approved leave.
5. Check-in/out writes are transactional and idempotency/conflict guarded. The unique employee/date constraint is the final guard against concurrent duplicate punches.
6. Attendance changes made by an administrator need an audit trail containing actor, old values, new values, and reason.

## Current implementation

The service currently implements check-in, check-out, employee history, paginated administrative search, date-range validation, Vietnam business time, shift snapshot plus late/early minute calculation for existing assignments, shift and assignment CRUD, and the unique employee/date database constraint. Check-out persists `work_duration_min` and `overtime_min`; shift end is snapshotted so overtime is measured against the assigned schedule, including overnight shifts. Without an active assignment the record is marked `NO_SHIFT`; work duration can still be measured from punches, but late and overtime calculations are unavailable. Shift data can be managed through `/api/shifts` and `/api/shift-assignments`. Absence/leave integration, monthly summaries, employee-service enrichment, grace periods, and admin correction auditing remain unimplemented. `work_duration_min` stores elapsed whole minutes as the source of truth; the response also exposes decimal `workDurationHours` for display.
