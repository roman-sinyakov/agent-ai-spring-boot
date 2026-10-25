# 4. Persistence: PostgreSQL (optional)

## Goal

PostgreSQL is an alternative to the default Firestore storage, for deployments that already have a relational database. It MUST follow the storage contract (`DB-1`…`DB-4`) from [specification 3](03-persistence-firestore.md), so the API behaves the same with either storage.

## Requirements

- **PG-1** PostgreSQL MUST be enabled only with the Spring profile `postgres`. Without the profile, data source, JPA and Flyway auto-configuration MUST stay off (DB-2).
- **PG-2** The schema MUST be created by a Flyway migration; Hibernate only validates it (`ddl-auto: validate`).
- **PG-3** Table `persons`:

  | Column | Type |
  |---|---|
  | `id` | `VARCHAR(36)` primary key (UUID from DB-4, no database sequence) |
  | `first_name`, `last_name` | `VARCHAR(100) NOT NULL` |
  | `email` | `VARCHAR(255) NOT NULL UNIQUE` |
  | `date_of_birth` | `DATE NOT NULL` |
  | `phone_number` | `VARCHAR(30) NOT NULL` |
  | `created_at`, `updated_at` | `TIMESTAMP NOT NULL` |

- **PG-4** Connection from `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`; JDBC time zone UTC.
- **PG-5** Sorting by last and first name is done by the database.
- **PG-6** E-mail uniqueness is checked before writing (case-insensitive) and additionally enforced by the unique constraint.

## Acceptance criteria

| # | Given | When | Then |
|---|---|---|---|
| AC-1 | profile `postgres`, empty database | the application starts | Flyway creates `persons` as in PG-3 and Hibernate validation passes |
| AC-2 | profile `postgres`, no Firestore configuration | the application starts | it starts and `/actuator/health/liveness` is `200` |
| AC-3 | profile `postgres` | the API test suite from [specification 1](01-person-management.md) runs against PostgreSQL 18 | all tests pass |
| AC-4 | a person was created | it is read back | `createdAt` and `updatedAt` are identical in both responses |

## Out of scope

- Other relational databases; H2 or other in-memory substitutes in tests.
- Connection pool tuning, read replicas.
