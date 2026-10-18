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
- `/api/tasks` for task assignment and manager review; `/api/tasks/me` for an employee's own tasks

Salary structure allowance details and formula metadata are stored as JSONB.
Pay slip detail snapshots are also JSONB so the UI can preserve breakdowns
that are not represented by the fixed total fields.

## Employee tasks and payroll bonus

Managers with `employee:task:manage` assign each task to a KPI month with a due
date, acceptance criteria, and weight. Employees with `employee:task:read` can
submit a proposed completion percentage, result description, and one or more
evidence URLs. A manager must accept or request supplementary evidence before
the approved percentage changes. Each submission and review is retained in the
task history. The employee identity for `/api/tasks/me` is resolved from the
authenticated employee profile.

Monthly task KPI is the sum of each accepted percentage multiplied by its task
weight, divided by the total weight of all tasks assigned to that employee for
the month. Tasks without an accepted percentage receive no credit until
verified. Managers can view all employee scores at `/api/tasks/kpi/monthly` and
employees can view their own at `/api/tasks/me/kpi/monthly`, both using
`periodMonth=YYYY-MM-01`. Apply
`docs/migrations/20261018_task_kpi_reward_penalty.sql` and then
`docs/migrations/20261018_task_acceptance_monthly_kpi.sql` to an existing
database. The migration keeps the legacy `completion_percent` column and copies
only previously approved values into the new `approved_percent` field; old
unapproved values remain available in the progress history.

Automatic payroll calculation adds bonuses for accepted task progress meeting
its KPI threshold to the assigned task month's pay slip bonus and taxable gross.
Accepted progress below threshold contributes the configured penalty to other
deductions. It records task counts and amounts in `snapshotData` for payroll
review. Task bonuses do not change the insurance base.

The datasource keeps the shared Supabase PostgreSQL connection from the base
project and can still be overridden with `DB_URL`, `DB_USERNAME`, and
`DB_PASSWORD`.

## Run with Maven

```bash
./mvnw spring-boot:run
```
