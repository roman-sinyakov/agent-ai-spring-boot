# PROJ-1: Acceptance tests (Cucumber)

**Jira:** PROJ-1 (placeholder – rename the file and this key when the ticket exists) · **Status:** In progress · **Created:** 2026-10-25 · **Done:** –

## Goal

The acceptance criteria of the person API and security specifications become executable Cucumber scenarios, readable without Java knowledge, traceable to requirements and run with every build. They complement the integration tests, they do not replace them.

## Requirements

- New: AT-1…AT-15 and AC-7…AC-13 in [05 Operations and delivery – Acceptance tests](../05-operations-and-delivery.md#acceptance-tests).
- Verified by the scenarios: acceptance criteria of [01 Person management](../01-person-management.md#acceptance-criteria) and [02 Security](../02-security.md#acceptance-criteria).

## Decisions

- **Firestore only.** Scenarios check behavior; both storages are already covered by the integration tests. Running them twice would double the build time.
- **Real HTTP and real tokens** (random port, `RestClient`, `/oauth2/token`) instead of MockMvc with mocked JWTs – this is what a client sees, and it covers the token endpoint and the 403 case that integration tests miss.
- **Gherkin in English**, like the rest of the repository.
- **Feature files by capability, not by task** (`person_management.feature`, `security.feature`); scenarios are linked to requirements with tags (`@01-AC-6 @API-11`), not to tickets.
- **Not against Render** – it has live data and would need a cleanup strategy; a possible later ticket.
- **Cucumber 7.34.9, not 8.** Cucumber 8 is built on JUnit 6 and Spring 7, i.e. Spring Boot 4. Cucumber 7.34 needs JUnit Platform 1.13+, so `junit-jupiter.version` is overridden to 5.14.4 (Spring Boot 3.5 manages 5.12). Both go away with an upgrade to Spring Boot 4.
- **Shared Firestore emulator.** `FirestoreEmulator` (test sources) starts one container for the integration and the acceptance tests.
- **Reports also on failure.** `store_test_results` and `store_artifacts` use `when: always`, because the reports matter most when tests fail.

## Plan

Steps are done in order, one commit each, following the [workflow](../README.md#workflow). A step is done only when `mvn clean verify` passes; tick it with the date (`– done <date>`) in the same commit.

- [x] **1. Cucumber skeleton with one scenario** (AT-6, AT-8, AT-9, AT-10, AT-11, AT-12)
  - Cucumber dependencies via the BOM, suite class, Spring context on a random port with the Firestore emulator, scenario-scoped state, empty register before each scenario.
  - First scenario: listing persons without a token (02-AC-3).
  - Done when: the scenario runs in `mvn clean verify`, and a deliberately broken step fails the build (05-AC-8). – done 2026-10-25

- [x] **2. Obtaining tokens** (AT-7, SEC-1, SEC-2)
  - Steps "the client has read / write access" requesting tokens from `/oauth2/token`.
  - Scenarios 02-AC-1 (token issued, `kid` in JWKS) and 02-AC-2 (wrong secret rejected).
  - Done when: both scenarios pass without mocked JWTs. – done 2026-10-25

- [x] **3. Access rules** (SEC-6…SEC-11)
  - Scenarios 02-AC-4…02-AC-8, including the read-only token on a write request (02-AC-6).
  - Done when: `security.feature` covers 02-AC-1…AC-8. – done 2026-10-25

- [x] **4. Creating and reading persons** (API-1, API-2, API-3, API-6, API-9, API-12)
  - Scenarios 01-AC-1…01-AC-4.
  - Done when: they pass and use only business-language steps (AT-3). – done 2026-10-25

- [x] **5. Validation, conflicts, update and delete** (API-4, API-5, API-8, API-10, API-11)
  - Scenarios 01-AC-5…01-AC-8.
  - Done when: `person_management.feature` covers 01-AC-1…AC-8. – done 2026-10-25

- [x] **6. Reports and tag filtering** (AT-2, AT-13, AT-14)
  - HTML report `target/cucumber-report.html` and JUnit XML results.
  - Done when: 05-AC-9, AC-10 and AC-11 are checked manually and noted here. – done 2026-10-25
  - Checked: every 01/02 `AC-n` is a tag; `@02-AC-6` runs 1 of 16 scenarios (15 skipped); the HTML report lists the tags; surefire XML has 16 test cases named `<feature> - <scenario>`. A failing and an undefined step both fail the build (05-AC-8).

- [ ] **7. CircleCI test reporting** (AT-14)
  - `store_test_results` and `store_artifacts` in the `build` job.
  - Done when: a pushed build shows the scenarios in *Tests* and the report in *Artifacts* (05-AC-12).
  - Configured and validated with `circleci config validate`; waits for the first push to be checked on CircleCI.

- [ ] **8. Documentation and closing** (AT-15)
  - README: how to run the scenarios and where the report is. – done 2026-10-25
  - Set the status to **Done** with the date here and in [specs/README.md](../README.md#tasks) – after step 7.

## Out of scope

See *Out of scope* in [specification 5](../05-operations-and-delivery.md#out-of-scope).
