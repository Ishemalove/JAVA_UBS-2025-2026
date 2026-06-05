# Utility Billing System ERD

Relational DBMS target: H2 for local development, PostgreSQL-compatible schema style for production.

```mermaid
erDiagram
    APP_USERS ||--o{ USER_ROLES : has
    APP_USERS ||--o{ VERIFICATION_TOKEN : receives
    APP_USERS ||--o{ OTP_TOKEN : receives
    APP_USERS ||--o{ REFRESH_TOKEN : owns

    CUSTOMER ||--o{ METER : owns
    METER ||--o{ METER_READING : records
    METER_READING ||--o| BILLS : generates
    CUSTOMER ||--o{ BILLS : receives
    BILLS ||--o{ PAYMENT : paid_by
    CUSTOMER ||--o{ NOTIFICATION : receives

    TARIFF ||--o{ TARIFF_TIER : contains
    AUDIT_LOG }o--|| APP_USERS : actor_email

    APP_USERS {
        uuid id PK
        string full_names
        string email UK
        string phone_number
        string password
        string status
        boolean email_verified
        datetime created_at
        datetime updated_at
    }
    CUSTOMER {
        uuid id PK
        string full_names
        string national_id UK
        string email
        string phone_number
        string address
        string status
    }
    METER {
        uuid id PK
        uuid customer_id FK
        string meter_number UK
        string meter_type
        date installation_date
        string status
    }
    METER_READING {
        uuid id PK
        uuid meter_id FK
        decimal previous_reading
        decimal current_reading
        date reading_date
        int billing_month
        int billing_year
    }
    TARIFF {
        uuid id PK
        string meter_type
        string mode
        int version
        string effective_from
        decimal flat_rate
        decimal fixed_charge
        decimal tax_rate
        decimal late_penalty_rate
    }
    BILLS {
        uuid id PK
        uuid customer_id FK
        uuid reading_id FK
        string reference UK
        decimal amount
        decimal outstanding_balance
        string status
        date due_date
    }
    PAYMENT {
        uuid id PK
        uuid bill_id FK
        decimal amount_paid
        string payment_method
        date payment_date
    }
    NOTIFICATION {
        uuid id PK
        uuid customer_id FK
        string message
        boolean sent
    }
```

Important constraints:

- `app_users.email`, `customer.national_id`, and `meter.meter_number` are unique.
- `meter_reading` has one reading per meter per `billing_month` and `billing_year`.
- Tariffs are versioned and new records must start from a future billing cycle.
- `data.sql` configures an H2 database trigger named `trg_bill_notification` that inserts a notification after a bill row is generated.
