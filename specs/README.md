# Specifications

Specifications of `agent-ai-spring-boot`, written as the input for building the application from scratch. They describe *what* the system does and the constraints it must respect; code structure is left to the implementation unless a decision is fixed here.

| # | Specification | Covers |
|---|---|---|
| 1 | [Person management API](01-person-management.md) | REST endpoints, validation, normalization, error format |
| 2 | [Security](02-security.md) | OAuth2 authorization server, JWT resource server, scopes |
| 3 | [Persistence](03-persistence.md) | Firestore (default) and PostgreSQL (optional) storage |
| 4 | [Operations and delivery](04-operations-and-delivery.md) | Configuration, health, tests, local run, Kubernetes, CI/CD, Render |

## Conventions

- Requirements have ids (`API-1`, `SEC-2`, ...) so tests, commits and reviews can refer to them.
- **MUST** is mandatory, **SHOULD** is expected unless there is a documented reason not to, **MAY** is optional.
- Acceptance criteria are written as *Given / When / Then* and each one maps to at least one automated test.
- Every specification ends with *Out of scope*: anything listed there must not be built without changing the specification first.

## Global constraints

These apply to every specification (see also `AGENTS.md`):

- Java 21, Spring Boot 3.5.x, Maven; latest versions of other dependencies.
- Maven group id and base package `pl.piomin.services`; artifact id `agent-ai-spring-boot` (the directory name).
- No Lombok. Minimal code: no layers or abstractions that a requirement does not need.
- Positive and negative tests for every behavior.
- CI-friendly version `${revision}` set by CircleCI to `MAJOR.MINOR.<pipeline number>`.
- README updated with every change of behavior or setup.
