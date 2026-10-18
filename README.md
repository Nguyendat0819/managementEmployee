# Notification Service

Notification microservice base cloned from the current Spring Boot base project.

This base intentionally contains no Notification API or business implementation. The
feature packages are ready for the next implementation pass.

The datasource keeps the shared Supabase PostgreSQL connection from the base
project and can still be overridden with `DB_URL`, `DB_USERNAME`, and
`DB_PASSWORD`.

## Run with Maven

```bash
./mvnw spring-boot:run
```
