# agent-ai-spring-boot

`agent-ai-spring-boot` is a Spring Boot 3 application that exposes a secured REST API for managing people. It persists data in PostgreSQL, protects API endpoints with OAuth2 client credentials and JWT bearer tokens, and includes local and Kubernetes deployment assets.

## Prompt

```text
Create the application that exposes a REST API and connects to a PostgreSQL database in the current directory.
The application should have a Person entity with an id and typical fields for each person.
All REST endpoints should be protected with JWT and OAuth2.
The codebase should use Skaffold to deploy on Kubernetes.
Don’t create a plan. 
Don’t ask for confirmation, just continue.
```

## Features

- REST API under `/api/v1/persons` (listing persons is public, other operations require a token)
- PostgreSQL persistence with Flyway migrations
- OAuth2 authorization server and JWT resource server in one application
- Health probes for Kubernetes
- Docker Compose for local development
- Skaffold and Kubernetes manifests for cluster deployment
- Positive and negative integration tests with JUnit 5 and MockMvc
- CircleCI pipeline: tests, image publishing to GitHub Container Registry and deployment to Render

## Prerequisites

- Java 21
- Maven 3.9+
- Docker and Docker Compose
- Kubernetes cluster with `kubectl`
- Skaffold

## Run Locally

Start PostgreSQL:

```bash
docker compose up -d postgres
```

Run the application:

```bash
mvn spring-boot:run
```

## Obtain a Token

Request an OAuth2 access token using the configured client:

```bash
curl -u person-api-client:changeit \
  -X POST http://localhost:8080/oauth2/token \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=client_credentials&scope=person.read%20person.write'
```

Use the returned `access_token` as a bearer token:

```bash
curl http://localhost:8080/api/v1/persons \
  -H "Authorization: Bearer ${TOKEN}"
```

## API Examples

Create a person:

```bash
curl -X POST http://localhost:8080/api/v1/persons \
  -H "Authorization: Bearer ${TOKEN}" \
  -H 'Content-Type: application/json' \
  -d '{
    "firstName": "Jane",
    "lastName": "Doe",
    "email": "jane.doe@example.com",
    "dateOfBirth": "1990-05-20",
    "phoneNumber": "+48123456789"
  }'
```

List people:

```bash
curl http://localhost:8080/api/v1/persons \
  -H "Authorization: Bearer ${TOKEN}"
```

## Build and Test

```bash
mvn clean verify
```

## Docker Compose

Run the full stack locally:

```bash
docker compose up --build
```

## Kubernetes with Skaffold

Deploy to the current cluster context:

```bash
skaffold run
```

For an iterative development loop:

```bash
skaffold dev
```

The Kubernetes manifests are stored in `k8s/`.

## CI/CD with CircleCI and Render

Current version: `1.0.3`.

The pipeline in `.circleci/config.yml` runs:

1. `build` – `mvn clean verify` on every branch.
2. `publish-image` – builds the image with Jib and pushes it to `ghcr.io/<github-user>/agent-ai-spring-boot` with the commit SHA and `latest` tags (`master` only).
3. `deploy` – calls the Render Deploy Hook, so Render pulls `latest` and restarts the service (`master` only).

### One-time setup

1. Create a GitHub personal access token (classic) with the `write:packages` scope.
2. Run the pipeline once so the image appears in GitHub Packages, then either make the package public or add registry credentials in Render.
3. In Render create a PostgreSQL database and a Web Service from the existing image `ghcr.io/<github-user>/agent-ai-spring-boot:latest` with these environment variables:

   | Variable | Value |
   |---|---|
   | `PORT` | `8080` |
   | `SPRING_DATASOURCE_URL` | `jdbc:postgresql://<host>:5432/<database>` from the Render database page |
   | `SPRING_DATASOURCE_USERNAME` | database user |
   | `SPRING_DATASOURCE_PASSWORD` | database password |
   | `APP_SECURITY_ISSUER` | public service URL, e.g. `https://agent-ai-spring-boot.onrender.com` |
   | `APP_SECURITY_CLIENT_ID` | OAuth2 client id |
   | `APP_SECURITY_CLIENT_SECRET` | OAuth2 client secret (do not use `changeit`) |

4. Copy the service Deploy Hook URL from Render (Settings → Deploy Hook).
5. In CircleCI (Project Settings → Environment Variables) add:
   - `GHCR_TOKEN` – the GitHub token from step 1,
   - `RENDER_DEPLOY_HOOK_URL` – the Deploy Hook URL from step 4.
