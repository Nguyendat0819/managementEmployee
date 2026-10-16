# Payroll Service

Payroll microservice base cloned from the current Spring Boot base project.

The service exposes APIs for salary structure management, payroll run setup,
pay slip registration, approval/payment status transitions, and the signed-in
employee's paid pay slips. It does not calculate attendance, overtime rates,
insurance, or tax yet. Pay slip components are supplied by the caller; the
service sums those components into Gross and Net totals.

## Payroll APIs

- `GET/POST /api/salary-structures`, `GET/PUT/DELETE /api/salary-structures/{id}`
- `GET/POST /api/payroll-runs`, `GET/DELETE /api/payroll-runs/{id}`
- `POST /api/payroll-runs/{id}/payslips` to register pre-calculated pay slips
- `GET /api/payroll-runs/{id}/payslips` to review a run
- `POST /api/payroll-runs/{id}/submit`, `/approve`, and `/mark-paid`
- `GET /api/payslips/me` and `GET /api/payslips/me/{id}` for paid pay slips

Salary structure allowance details and formula metadata are stored as JSONB.
Pay slip detail snapshots are also JSONB so the UI can preserve breakdowns
that are not represented by the fixed total fields.

The datasource keeps the shared Supabase PostgreSQL connection from the base
project and can still be overridden with `DB_URL`, `DB_USERNAME`, and
`DB_PASSWORD`.

## Run with Maven

```bash
./mvnw spring-boot:run
```
