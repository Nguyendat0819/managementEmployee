# Leave Service

Leave microservice base cloned from the current Spring Boot base project.

The service exposes APIs for employee leave requests, approvals, leave balances,
leave types, and holidays. New leave requests use the authenticated JWT subject
and employee code claim; clients cannot choose another employee's identity in
the request body.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/api/leave-requests` | Submit a leave request |
| `GET` | `/api/leave-requests/me` | List the signed-in employee's requests |
| `PATCH` | `/api/leave-requests/me/{id}/cancel` | Cancel a pending request owned by the employee |
| `GET` | `/api/leave-requests` | Search and paginate requests for approval |
| `PATCH` | `/api/leave-requests/{id}/approve` | Approve a pending request |
| `PATCH` | `/api/leave-requests/{id}/reject` | Reject a pending request with a reason |
| `GET` | `/api/leave-balances/me?year=2026` | Read the signed-in employee's yearly balances |
| `GET/POST/PUT/DELETE` | `/api/leave-types` | List and manage leave types |
| `GET/POST/PUT/DELETE` | `/api/holidays` | List and manage holidays |

Apply `docs/migrations/20261018_leave_service_workflow.sql` before deploying to
an environment that does not run Hibernate schema updates. The service uses the
existing `employee:leave:read`, `employee:leave:approve`, and
`employee:leave:manage` permissions.

Leave duration counts weekdays and excludes configured holidays. A request
must fit within one calendar year and its leave type's available balance. Leave
types can carry forward up to their configured carryover limit from the prior
year's remaining balance.

The datasource keeps the shared Supabase PostgreSQL connection from the base
project and can still be overridden with `DB_URL`, `DB_USERNAME`, and
`DB_PASSWORD`.

## Run with Maven

```bash
./mvnw spring-boot:run
```
