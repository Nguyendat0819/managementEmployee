# Attendance Service

Attendance microservice cloned from the current Spring Boot base project.

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
```
