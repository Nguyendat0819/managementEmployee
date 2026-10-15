# Attendance Service

Spring Boot service for employee check-in/check-out, attendance history, and attendance administration. The API is mounted at `/attendance-service` and uses the shared JWT resource-server configuration and `ApiResponse` envelope.

## Run with Maven

```bash
./mvnw spring-boot:run
```

## API

The service uses the shared JWT secret when integrated with the API Gateway.

```text
POST /attendance-service/api/attendance/check-in
POST /attendance-service/api/attendance/check-out
GET  /attendance-service/api/attendance/history?employeeCode=NV-000128&from=2026-01-01&to=2026-12-31
GET  /attendance-service/api/attendance/records?from=2026-10-01&to=2026-10-31&status=PRESENT&page=0&size=20
```

`history` returns an employee's records in a date range. `records` is the paginated administrative view and accepts optional `employeeCode` and `status` filters. Dates use ISO `yyyy-MM-dd`; page is zero-based and size is limited to 200. Responses wrap data in the shared `{ transactionTime, code, message, traceId, data }` format.

Attendance status values include `PRESENT`, `LATE`, `INCOMPLETE`, and `NO_SHIFT`. When an active shift assignment exists, check-in stores the scheduled shift snapshot and the number of minutes early or late. Check-out persists elapsed work minutes in `work_duration_min`; `overtime_min` is calculated from the scheduled shift end. Without an active shift assignment, the service marks the record `NO_SHIFT` and does not claim an overtime calculation. The API also returns `workDurationHours` for display. All business dates and timestamps use `Asia/Ho_Chi_Minh`. The database enforces one record per employee per recorded date, while check-out looks up the latest open record to support overnight shifts.

## Backend design boundary

See [the backend design](docs/backend-design.md) for the service boundary, proposed shift/assignment and summary APIs, data model, permission codes, and attendance rules.

- `attendance_records` is the source of truth for daily punches and calculated duration.
- `shifts` and `employee_shift_assignments` can be managed through `/api/shifts` and `/api/shift-assignments`. The admin FE shift and assignment pages use these APIs. An employee with no effective shift assignment receives `NO_SHIFT`, and overtime is left unclaimed until a shift is assigned.
- Employee self-service endpoints should resolve the employee identity from a trusted JWT claim before production rollout; the current request DTO still accepts `employeeCode` so the existing client contract remains usable.
- Admin endpoints should be restricted through the service's permission annotations when the attendance permission codes are registered with the shared admin/role service.
