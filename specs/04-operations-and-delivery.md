# 4. Operations and delivery

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
  | `GOOGLE_CLOUD_PROJECT`, `GOOGLE_APPLICATION_CREDENTIALS`, `FIRESTORE_EMULATOR_HOST` | Firestore, see DB-10 | – |
  | `SPRING_DATASOURCE_URL`, `_USERNAME`, `_PASSWORD` | PostgreSQL, see DB-14 | `jdbc:postgresql://localhost:5432/persondb`, `person`, `person` |

- **OPS-2** Secrets (client secret, service account key, database password) MUST NOT be committed except for local development defaults.

### Health

- **OPS-3** Liveness and readiness probes MUST be available at `/actuator/health/liveness` and `/actuator/health/readiness`; only `health` and `info` actuator endpoints are exposed.

### Tests

- **OPS-4** Integration tests MUST run the API through the full Spring context (MockMvc, security included) against real storage engines: the Firestore emulator and PostgreSQL 18.
- **OPS-5** Locally the tests MUST start the Firestore emulator and PostgreSQL with Testcontainers (only Docker needed). When `FIRESTORE_EMULATOR_HOST` or `SPRING_DATASOURCE_URL` is set, the tests MUST use that instance instead (used in CI).
- **OPS-6** One test suite defines the API scenarios; it runs once per storage.

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

## Out of scope

- Ingress, TLS and autoscaling in Kubernetes; infrastructure as code for Render (`render.yaml`).
- Monitoring, metrics and centralized logging.
