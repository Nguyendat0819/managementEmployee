# Notification Service

Notification microservice base cloned from the current Spring Boot base project.

The service provides a persisted in-app notification inbox and APIs to create,
list, and mark notifications as read. Notifications are scoped to the numeric
JWT subject (`sub`) so a user can only read or update their own notifications.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/api/notifications/me` | Return the latest 100 notifications for the signed-in user |
| `GET` | `/api/notifications/me/unread-count` | Return the unread count |
| `PATCH` | `/api/notifications/me/{id}/read` | Mark one owned notification as read |
| `PATCH` | `/api/notifications/me/read-all` | Mark all owned notifications as read |
| `POST` | `/api/notifications` | Create a notification; requires `employee:notification:create` |

Apply `docs/migrations/20261018_add_notification_inbox_fields.sql` before
deploying to an environment that does not run Hibernate schema updates.

The datasource keeps the shared Supabase PostgreSQL connection from the base
project and can still be overridden with `DB_URL`, `DB_USERNAME`, and
`DB_PASSWORD`.

## Run with Maven

```bash
./mvnw spring-boot:run
```
