# 2. Security

## Goal

The API is protected with OAuth2: machine clients obtain a JWT access token with the client credentials grant and send it as a bearer token. The application issues and validates the tokens itself, so no external identity provider is needed.

## Requirements

### Token issuing (authorization server)

- **SEC-1** The application MUST act as an OAuth2 authorization server (Spring Authorization Server) exposing at least:
  - `POST /oauth2/token` – issues tokens,
  - `GET /oauth2/jwks` – public keys for verifying token signatures,
  - `GET /.well-known/oauth-authorization-server` – server metadata.
- **SEC-2** Exactly one client MUST be registered, supporting only the `client_credentials` grant, authenticated with HTTP Basic (`client_id:client_secret`), and allowed the scopes `person.read` and `person.write`.
- **SEC-3** The client id, client secret and token issuer (`iss`) MUST be configurable (see [Operations](05-operations-and-delivery.md#configuration)); the secret MUST be stored hashed in memory.
- **SEC-4** Access tokens MUST be JWTs signed with RS256. The RSA key pair (2048 bits) is generated at startup and kept in memory only.
  - Consequence (accepted): after a restart previously issued tokens are rejected, and several instances do not accept each other's tokens.

### Token validation (resource server)

- **SEC-5** Every protected request MUST carry `Authorization: Bearer <jwt>`. The signature, expiry and issuer are validated with the application's own key; token scopes map to authorities `SCOPE_<scope>`.

### Access rules

| Id | Request | Access |
|---|---|---|
| SEC-6 | `GET /api/v1/persons` | public |
| SEC-7 | `GET /api/v1/persons/{id}` | `person.read` |
| SEC-8 | `POST`, `PUT`, `DELETE /api/v1/persons/**` | `person.write` |
| SEC-9 | `GET /actuator/health/**` | public |
| SEC-10 | any other request (e.g. `/actuator/info`) | any valid token |

- **SEC-11** A missing or invalid token on a protected request MUST return `401`; a valid token without the required scope MUST return `403`.
- **SEC-12** CSRF protection MUST be disabled for the API (stateless bearer tokens, no browser sessions).

## Acceptance criteria

| # | Given | When | Then |
|---|---|---|---|
| AC-1 | correct client credentials | `POST /oauth2/token` with `grant_type=client_credentials&scope=person.read person.write` | `200`, `access_token` is a JWT whose header `kid` is listed in `/oauth2/jwks` |
| AC-2 | wrong client secret | `POST /oauth2/token` | `401` |
| AC-3 | no token | `GET /api/v1/persons` | `200` |
| AC-4 | no token | `GET /api/v1/persons/1` | `401` |
| AC-5 | no token | `POST /api/v1/persons` | `401` |
| AC-6 | token with only `person.read` | `POST /api/v1/persons` | `403` |
| AC-7 | no token | `GET /actuator/health/liveness` | `200` |
| AC-8 | no token | `GET /actuator/info` | `401` |

## Out of scope

- User login, authorization code flow, refresh tokens, multiple clients or dynamic client registration.
- Persisting signing keys or tokens; key rotation.
