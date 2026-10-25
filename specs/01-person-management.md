# 1. Person management API

## Goal

Clients manage a register of people over a JSON REST API: list, read, create, update and delete a person.

## Data model

A person has:

| Field | Type | Rules |
|---|---|---|
| `id` | string | Assigned by the server on create (UUID), never changes, read-only for clients |
| `firstName` | string | Required, not blank, max 100 characters |
| `lastName` | string | Required, not blank, max 100 characters |
| `email` | string | Required, valid e-mail, max 255 characters, unique (case-insensitive) |
| `dateOfBirth` | date `YYYY-MM-DD` | Required, in the past |
| `phoneNumber` | string | Required, 7–20 characters from `+ 0-9 ( ) space -` |
| `createdAt` | timestamp (ISO-8601, UTC) | Set by the server on create, never changes |
| `updatedAt` | timestamp (ISO-8601, UTC) | Set by the server on create and on every update |

## Requirements

### Endpoints

All endpoints are under `/api/v1/persons`. Access rules are defined in [Security](02-security.md).

| Id | Method and path | Success | Errors |
|---|---|---|---|
| API-1 | `GET /api/v1/persons` | `200`, array of persons | – |
| API-2 | `GET /api/v1/persons/{id}` | `200`, the person | `404` |
| API-3 | `POST /api/v1/persons` | `201`, the created person, `Location: /api/v1/persons/{id}` | `400`, `409` |
| API-4 | `PUT /api/v1/persons/{id}` | `200`, the updated person | `400`, `404`, `409` |
| API-5 | `DELETE /api/v1/persons/{id}` | `204`, empty body | `404` |

- **API-6** The list MUST be sorted by `lastName`, then `firstName`.
- **API-7** `POST` and `PUT` take the body `{firstName, lastName, email, dateOfBirth, phoneNumber}`; any `id`, `createdAt` or `updatedAt` in the body MUST be ignored.
- **API-8** `PUT` replaces all editable fields; `createdAt` MUST stay unchanged and `updatedAt` MUST be set to the current time.

### Normalization

- **API-9** Before saving, `firstName`, `lastName` and `phoneNumber` MUST be trimmed, `email` MUST be trimmed and lower-cased. Responses return the normalized values.

### Validation and errors

- **API-10** A body that violates the data model rules MUST be rejected with `400` and no data changed.
- **API-11** Creating a person with an e-mail that another person already has (case-insensitive), or updating a person to such an e-mail, MUST be rejected with `409`. Updating a person while keeping their own e-mail MUST succeed.
- **API-12** An unknown id MUST return `404` for read, update and delete. Any string is accepted as an id; one that does not exist is simply not found.
- **API-13** Errors produced by the API (`400` validation, `404`, `409`) MUST use this body:

  ```json
  {
    "timestamp": "2026-10-25T10:15:30Z",
    "status": 404,
    "error": "Not Found",
    "message": "Person with id 'abc' was not found",
    "details": {}
  }
  ```

  - `404` message: `Person with id '<id>' was not found`
  - `409` message: `A person with email '<normalized email>' already exists`
  - `400` message: `Validation failed`; `details` maps every invalid field to its validation message.

## Acceptance criteria

| # | Given | When | Then |
|---|---|---|---|
| AC-1 | a valid body and write access | `POST /api/v1/persons` | `201`, body has a string `id`, `Location` points to the new person, e-mail is lower-cased |
| AC-2 | persons "Smith John" and "Doe Jane" | `GET /api/v1/persons` | `200`, Doe before Smith |
| AC-3 | an existing person and read access | `GET /api/v1/persons/{id}` | `200` with the same data as returned on create |
| AC-4 | no person with id `999` and read access | `GET /api/v1/persons/999` | `404`, message `Person with id '999' was not found` |
| AC-5 | body with blank `firstName`, invalid `email`, future `dateOfBirth`, `phoneNumber` `abc` | `POST /api/v1/persons` | `400`, `details` contains at least `firstName` and `email` |
| AC-6 | a person with `jane.doe@example.com` | `POST` another person with `Jane.Doe@example.com` | `409` |
| AC-7 | an existing person | `PUT` with changed `lastName` and the same e-mail | `200`, new `lastName`, unchanged `createdAt`, newer `updatedAt` |
| AC-8 | an existing person and write access | `DELETE /api/v1/persons/{id}`, then `GET` it | `204`, then `404` |

## Out of scope

- Pagination, filtering and search on the list endpoint.
- Partial update (`PATCH`), optimistic locking, audit history.
- Bulk operations.
