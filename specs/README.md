# Specifications

Specifications of `agent-ai-spring-boot`. They describe *what* the system does and the constraints it must respect; code structure is left to the implementation unless a decision is fixed here.

## Structure

```
specs/
  01-…05-*.md     the specifications of the system
  tasks/
    <JIRA-KEY>.md  one file per Jira ticket: the mini-specification of the task,
                   the plan for the execution loop and, once done, the journal entry
```

### Specifications

| # | Specification | Covers |
|---|---|---|
| 1 | [Person management API](01-person-management.md) | REST endpoints, validation, normalization, error format |
| 2 | [Security](02-security.md) | OAuth2 authorization server, JWT resource server, scopes |
| 3 | [Persistence: Firestore](03-persistence-firestore.md) | Storage contract shared by all storages, Firestore as the default storage |
| 4 | [Persistence: PostgreSQL](04-persistence-postgresql.md) | Optional PostgreSQL storage |
| 5 | [Operations and delivery](05-operations-and-delivery.md) | Configuration, health, tests (integration and Cucumber acceptance tests), local run, Kubernetes, CI/CD, Render |

### Tasks

| Ticket | Title | Status |
|---|---|---|
| [PROJ-1](tasks/PROJ-1.md) | Acceptance tests (Cucumber) | In progress |

## Task file

`tasks/<JIRA-KEY>.md`, e.g. `tasks/PROJ-123.md`:

```markdown
# PROJ-123: <title>

**Jira:** <link> · **Status:** Planned | In progress | Done · **Created:** <date> · **Done:** <date>

## Goal
Why, and what changes for the user.

## Requirements
Ids of new or changed requirements in the specifications (drafted here while discussing,
moved to the specifications on approval) and the acceptance criteria the task must satisfy.

## Decisions
Questions discussed, what was decided and why.

## Plan
- [ ] **1. <step>** (<requirement ids>) – done when …

## Out of scope
```

The file holds the content (goal, requirements, decisions, plan); Jira holds the organization (status, assignee, sprint) and links to the file. Jira sub-tasks, if used, are referenced from plan steps; they get no files of their own.

## Workflow

1. **Ticket and specification together.** A Jira ticket gets its file `tasks/<KEY>.md`. New or changed requirements are drafted there while discussing; on approval they are written into the specifications and the task file refers to their ids. No code until you approve. The specification changes are committed together with the code of the task (in its branch), so the specifications in `master` always describe what is built.
2. **Plan.** The *Plan* section splits the work into small, vertical steps (test + code + docs), each referring to requirement ids and stating when it is done.
3. **Execute** one step at a time:
   1. write the test for the referenced acceptance criteria;
   2. implement the minimum to make it pass;
   3. run `mvn clean verify`;
   4. update README and other docs if needed;
   5. review with the `code-reviewer` agent (see `AGENTS.md`);
   6. tick the step, append the date (`– done 2026-10-25`) and commit everything together; the commit message starts with the ticket key, e.g. `PROJ-123: Access rule scenarios (SEC-6…SEC-11)`, so commits are found with `git log --grep "PROJ-123"` and linked by the Jira integration.

   Stop and ask instead of guessing when the specification is ambiguous, when a test cannot be made green after a few attempts, or when the review finds a problem that does not fit the step.
4. **Close.** Set the status to **Done** with the date in the task file and in the table above. Task files are never deleted: with the specifications and the commit history they are the journal of what changed, when and why.

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
