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

- REST API under `/api/v1/persons`
- PostgreSQL persistence with Flyway migrations
- OAuth2 authorization server and JWT resource server in one application
- Health probes for Kubernetes
- Docker Compose for local development
- Skaffold and Kubernetes manifests for cluster deployment
- Positive and negative integration tests with JUnit 5 and MockMvc

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
