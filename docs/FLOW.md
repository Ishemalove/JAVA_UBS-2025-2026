# Spring Boot Flow Diagram

```mermaid
flowchart TD
    Client["Postman / Swagger UI"] --> Controller["Controller Layer"]
    Controller --> Validation["DTO Validation"]
    Validation --> Security["JWT Filter + Method Role Checks"]
    Security --> Service["Service Layer"]
    Service --> Rules["Business Rules"]
    Rules --> Repository["Spring Data JPA Repositories"]
    Repository --> Database["Relational Database"]
    Database --> Trigger["DB Trigger: bill notification"]
    Service --> Email["Email Service"]
    Service --> Audit["Audit Log"]
```

Authentication flow:

```mermaid
sequenceDiagram
    participant Client
    participant AuthController
    participant AuthService
    participant DB
    participant Email

    Client->>AuthController: POST /api/auth/register
    AuthController->>AuthService: validate DTO
    AuthService->>DB: save inactive user, verification token, OTP
    AuthService->>Email: welcome + verification email
    Client->>AuthController: POST /api/auth/verify-email
    AuthService->>DB: mark email verified
    Client->>AuthController: POST /api/auth/verify-otp/{email}
    AuthService->>DB: activate account
    Client->>AuthController: POST /api/auth/login
    AuthService-->>Client: access JWT + refresh token
```

Billing flow:

```mermaid
sequenceDiagram
    participant Operator
    participant Finance
    participant Service
    participant DB
    participant Customer

    Operator->>Service: Capture meter reading
    Service->>DB: Store reading if meter active and month unique
    Finance->>Service: Generate bill
    Service->>DB: Store bill
    DB-->>DB: Trigger inserts notification
    Finance->>Service: Record partial/full payment
    Service->>DB: Update outstanding balance and bill status
    Service->>Customer: Send notification on full payment
```

Role access summary:

- `ROLE_ADMIN`: manage users, customers, meters, tariffs, bill approvals.
- `ROLE_OPERATOR`: capture meter readings and view operational customer/meter data.
- `ROLE_FINANCE`: approve bills and record payments.
- `ROLE_CUSTOMER`: view bills, payments, and notifications.

Run:

```powershell
mvn spring-boot:run
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

Seeded administrator:

```text
email: admin@utility.rw
password: Admin@123!
```
