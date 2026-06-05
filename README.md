# Utility Billing System

Spring Boot backend for WASAC/REG-style postpaid utility billing.

## Features

- JWT authentication, refresh tokens, logout through token blacklist.
- Gmail SMTP is configured so OTP, verification, credential, role-change, reset, and notification emails can be sent from `UBS system <ishemalove@gmail.com>`.
- BCrypt password hashing.
- Email verification token and OTP verification.
- Forgot password and reset password with OTP.
- Role-based authorization for `ROLE_ADMIN`, `ROLE_OPERATOR`, `ROLE_FINANCE`, and `ROLE_CUSTOMER`.
- DTO validation for all requests, including lowercase email format and strong passwords.
- Customer, meter, reading, tariff, bill, payment, notification, and user APIs.
- Pagination, sorting, and search on major list endpoints.
- Global exception handling with clear error messages.
- Swagger/OpenAPI documentation with allowed roles in operation summaries.
- Swagger tags are ordered by workflow: authentication, users/roles, customers, meters, readings, tariffs, bills, payments, notifications.
- Audit fields on all entities plus `AuditLog` records for important actions.
- H2 database trigger that creates a notification when a bill is generated.

## Run

Create the PostgreSQL database first:

```sql
CREATE DATABASE ubs;
```

If your PostgreSQL password is not empty, start with environment variables:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/ubs"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your_postgres_password"
```

```powershell
mvn spring-boot:run
```

Open Swagger at:

```text
http://localhost:8080/swagger-ui.html
```

Seeded admin:

```text
email: admin@utility.rw
password: Admin@123!
```

## Notes

- Email sending uses Gmail SMTP through Spring `JavaMailSender`; failed delivery returns a clear API error.
- Administrators can create staff accounts at `POST /api/users/system`. The system generates a temporary password, emails it to the user, and forces password change before business endpoints can be used.
- The app uses PostgreSQL database `ubs` by default. Data persists after restart.
- Tariff `effectiveFrom` must be sent as a string in `YYYY-MM` format, for example `"2026-07"`, not as an object.
- See `docs/ERD.md` and `docs/FLOW.md` for the required diagrams.

## Tariff Example

```json
{
  "meterType": "WATER",
  "mode": "FLAT",
  "effectiveFrom": "2026-07",
  "flatRate": 500,
  "fixedCharge": 1000,
  "taxRate": 18,
  "latePenaltyRate": 2,
  "tiers": []
}
```
