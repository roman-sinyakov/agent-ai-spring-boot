# Claude Code Configuration for claude-ai-spring-boot

> AI-powered development workspace configuration

## Skills

Load all skills from `.agents/skills/`.

To use a skill, load it first, then invoke with natural language.
Each skill is one independent capability.

When solving problems:
1. Search there first
2. Extend existing skills
3. Do not bypass them

## Project General Instructions

- Always use the latest versions of dependencies.
- Always write Java code as the Spring Boot application.
- Always use Maven for dependency management.
- Always create test cases for the generated code both positive and negative.
- Always generate the CircleCI pipeline in the .circleci directory to verify the code.
- Minimize the amount of code generated.
- The Maven artifact name must be the same as the parent directory name.
- Use semantic versioning for the Maven project. The version is a CI-friendly `${revision}`: CircleCI sets it to `MAJOR.MINOR.<pipeline number>`, so do not bump PATCH manually. Change MAJOR or MINOR only in the `REVISION` variable in `.circleci/config.yml`.
- Use `pl.piomin.services` as the group ID for the Maven project and base Java package.
- Do not use the Lombok library.
- Generate the Docker Compose file to run all components used by the application.
- Update README.md each time you generate a new version.

## Code Review

- Before every commit, review the changes with the `code-reviewer` agent (`.claude/agents/code-reviewer.md`), not in the current conversation.
- Pass it the scope (by default the uncommitted changes) and the deliberate design decisions made for these changes, so they are not reported as defects.
- Fix Critical and Major findings before committing, or explain to the user why a finding is not fixed. Report Minor findings to the user and let them decide.
- Do not commit until the user has seen the review result.