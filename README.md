# API Gateway

The gateway is the single HTTP entry point for the HRM services. It routes the
existing service context paths and validates the shared HS256 JWT without
connecting to PostgreSQL.

Run locally:

```bash
mvn spring-boot:run
```

Gateway URL:

```text
http://localhost:8088
```

Routes keep the existing public context paths:

- `/employee-service/**`
- `/attendance-service/**`
- `/payroll-service/**`
- `/leave-service/**`
- `/notification-service/**`
