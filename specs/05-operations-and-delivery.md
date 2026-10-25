# 5. Operations and delivery

## Goal

The application is configured only through environment variables, reports its health to the platform, is verified automatically on every change and is delivered to Render; it can also run locally with Docker Compose and in Kubernetes with Skaffold.

## Requirements

### Configuration

- **OPS-1** All environment-specific settings MUST come from environment variables with local defaults:

  | Variable | Purpose | Default |
  |---|---|---|
  | `SERVER_PORT` | HTTP port | `8080` |
  | `APP_SECURITY_CLIENT_ID` | OAuth2 client id | `person-api-client` |
  | `APP_SECURITY_CLIENT_SECRET` | OAuth2 client secret | `changeit` (local only) |
  | `APP_SECURITY_ISSUER` | token issuer, the public URL | `http://localhost:8080` |
  | `SPRING_PROFILES_ACTIVE` | `postgres` selects PostgreSQL | none (Firestore) |
  | `GOOGLE_CLOUD_PROJECT`, `GOOGLE_APPLICATION_CREDENTIALS`, `FIRESTORE_EMULATOR_HOST` | Firestore, see FS-6 | – |
  | `SPRING_DATASOURCE_URL`, `_USERNAME`, `_PASSWORD` | PostgreSQL, see PG-4 | `jdbc:postgresql://localhost:5432/persondb`, `person`, `person` |

- **OPS-2** Secrets (client secret, service account key, database password) MUST NOT be committed except for local development defaults.

### Health

- **OPS-3** Liveness and readiness probes MUST be available at `/actuator/health/liveness` and `/actuator/health/readiness`; only `health` and `info` actuator endpoints are exposed.

### Tests

- **OPS-4** Integration tests MUST run the API through the full Spring context (MockMvc, security included) against real storage engines: the Firestore emulator and PostgreSQL 18.
- **OPS-5** Locally the tests MUST start the Firestore emulator and PostgreSQL with Testcontainers (only Docker needed). When `FIRESTORE_EMULATOR_HOST` or `SPRING_DATASOURCE_URL` is set, the tests MUST use that instance instead (used in CI).
- **OPS-6** One test suite defines the API scenarios; it runs once per storage.

### Acceptance tests

The acceptance criteria of the [person API](01-person-management.md) and [security](02-security.md) specifications become executable Cucumber scenarios in Gherkin, readable without Java knowledge and traceable to requirements. They complement the integration tests (OPS-4…OPS-6), they do not replace them:

| | Integration tests | Acceptance tests |
|---|---|---|
| Audience | developers | anyone reading the specifications |
| Access to the application | MockMvc, mocked JWT (`jwt()`) | real HTTP on a random port, real token from `/oauth2/token` |
| Storage | Firestore and PostgreSQL | Firestore only (the default) |
| Checks storage contents | yes | no, only observable API behavior |

- **AT-1** Every acceptance criterion of specifications 1 and 2 MUST have at least one scenario:

  | Feature file | Acceptance criteria |
  |---|---|
  | `person_management.feature` | [01](01-person-management.md#acceptance-criteria) AC-1…AC-8 |
  | `security.feature` | [02](02-security.md#acceptance-criteria) AC-1…AC-8 |

  Acceptance criteria of specifications 3–5 stay in JUnit integration tests.
- **AT-2** Each scenario MUST be tagged with the acceptance criterion and the requirements it verifies, in the form `@<spec>-AC-<n>` and `@<requirement id>`, e.g. `@01-AC-6 @API-11`.
- **AT-3** Scenarios MUST be written in business language: they describe people, e-mails and access rights, not JSON paths or Java. HTTP status codes MAY appear where the specification defines them.
- **AT-4** Scenarios MUST be independent: each one starts with an empty person register and creates its own data in `Given` steps.
- **AT-5** Common preconditions (e.g. "the client has write access") MUST be reusable steps; repeated data SHOULD use `Background` or `Scenario Outline` instead of copies.

  ```gherkin
  @01-AC-6 @API-11
  Scenario: E-mail must be unique regardless of letter case
    Given the client has write access
    And a person "Jane Doe" with e-mail "jane.doe@example.com" exists
    When the client creates a person "Joanna Doe" with e-mail "Jane.Doe@example.com"
    Then the request is rejected as a conflict
  ```

- **AT-6** Scenarios MUST call the application over real HTTP: `@SpringBootTest(webEnvironment = RANDOM_PORT)` and Spring's `RestClient`, no additional HTTP library.
- **AT-7** Access tokens MUST be obtained from `POST /oauth2/token` with the configured test client and the scopes a step asks for; JWTs MUST NOT be mocked.
- **AT-8** The application runs with the Firestore emulator, provided as for integration tests (OPS-5).
- **AT-9** State shared between steps of one scenario (last response, token, created ids) MUST be kept in a scenario-scoped bean, never in static fields.
- **AT-10** Cucumber JVM with the JUnit Platform engine and Spring integration (`cucumber-java`, `cucumber-spring`, `cucumber-junit-platform-engine`, `junit-platform-suite`), managed by the Cucumber BOM, in the latest versions compatible with Spring Boot 3.5: Cucumber 7.x (Cucumber 8 requires JUnit 6 and Spring 7) and JUnit 5.14 instead of the 5.12 managed by Spring Boot (Cucumber 7.34 requires JUnit Platform 1.13+).
- **AT-11** Feature files in `src/test/resources/features/`; suite class, Spring configuration and step definitions in the package `pl.piomin.services.acceptance` under `src/test/java`.
- **AT-12** `mvn clean verify` MUST run the scenarios together with the other tests; a failed or undefined step MUST fail the build.
- **AT-13** Scenarios MUST be selectable by tag, e.g. `mvn test -Dtest=AcceptanceTest -Dcucumber.filter.tags="@API-11"`.
- **AT-14** The build MUST produce an HTML report (`target/cucumber-report.html`) and JUnit XML results; the CircleCI `build` job MUST store the test results (`store_test_results`) and the HTML report (`store_artifacts`).
- **AT-15** README MUST describe how to run the acceptance tests and where to find the report.

### Local run

- **OPS-7** `docker-compose.yml` MUST run the application with the Firestore emulator by default; PostgreSQL is an optional Compose profile `postgres`, started with `SPRING_PROFILES_ACTIVE=postgres docker compose --profile postgres up`.
- **OPS-8** An HTTP client file (`person-api.http` with `http-client.env.json`) MUST cover the token request, JWKS and all API calls including negative cases, for the `local` and `render` environments.

### Kubernetes

- **OPS-9** Kubernetes manifests in `k8s/` and `skaffold.yaml` MUST deploy the application and PostgreSQL; the image is built with Jib.
- **OPS-10** In Kubernetes the application MUST use the external Firestore by default. The project id and the key file come from a manually created secret `firebase-credentials` (keys `project-id`, `firebase-key.json`), mounted at `/etc/firebase/firebase-key.json`. The secret is optional so that the pod also starts in PostgreSQL mode.
- **OPS-11** PostgreSQL 18 data MUST be mounted at `/var/lib/postgresql` (the 18+ image refuses a mount at `/var/lib/postgresql/data`).

### CI/CD

- **OPS-12** CircleCI (`.circleci/config.yml`) MUST run three jobs:
  1. `build` on every branch: `mvn clean verify` with the Firestore emulator and PostgreSQL as service containers;
  2. `publish-image` on `master`: Jib pushes `ghcr.io/<user>/agent-ai-spring-boot` tagged with the commit SHA, the version and `latest`;
  3. `deploy` on `master`: calls the Render deploy hook.
- **OPS-13** The version MUST be `MAJOR.MINOR.<pipeline number>`; `MAJOR.MINOR` is defined once, in the `REVISION` variable of the CircleCI config.
- **OPS-14** Render runs the `latest` image as a web service. Firestore credentials are provided as the Secret File `firebase-key.json` (available at `/etc/secrets/firebase-key.json`) plus `GOOGLE_CLOUD_PROJECT` and `GOOGLE_APPLICATION_CREDENTIALS`.

## Acceptance criteria

| # | Given | When | Then |
|---|---|---|---|
| AC-1 | Docker running, no environment variables | `mvn clean verify` | all tests pass for both storages |
| AC-2 | a commit on any branch | CircleCI runs | `build` passes; nothing is published |
| AC-3 | a commit on `master` | CircleCI runs | the image is in ghcr.io with SHA, version and `latest` tags and Render redeploys it |
| AC-4 | `docker compose up --build` | `GET http://localhost:8080/api/v1/persons` | `200` |
| AC-5 | Docker Desktop Kubernetes and the `firebase-credentials` secret | `skaffold dev` | the `person-api` pod becomes ready and the API is reachable at `localhost:8080` |
| AC-6 | the PostgreSQL pod restarts | – | data is kept (persistent volume) |
| AC-7 | Docker running | `mvn clean verify` | all acceptance scenarios run and pass (AT-12) |
| AC-8 | a step fails or has no definition | `mvn clean verify` | the build fails and names the scenario (AT-12) |
| AC-9 | the feature files | every `AC-n` of specifications 1 and 2 is searched for as a tag | each one is found at least once (AT-1, AT-2) |
| AC-10 | `-Dcucumber.filter.tags="@02-AC-6"` | `mvn test -Dtest=AcceptanceTest` | only the scenario for a read-only token on a write request runs (AT-13) |
| AC-11 | a build has finished | – | `target/cucumber-report.html` lists all scenarios with their tags (AT-14) |
| AC-12 | a CircleCI `build` job has finished | the job page is opened | *Tests* shows the scenarios and the HTML report is in *Artifacts* (AT-14) |
| AC-13 | scenarios run in any order | – | results are the same (AT-4) |

## Out of scope

- Ingress, TLS and autoscaling in Kubernetes; infrastructure as code for Render (`render.yaml`).
- Monitoring, metrics and centralized logging.
- Acceptance scenarios against PostgreSQL or deployed environments (Render, Kubernetes); scenarios for persistence format, startup and deployment; Gherkin in other languages, living documentation sites, parallel execution.
